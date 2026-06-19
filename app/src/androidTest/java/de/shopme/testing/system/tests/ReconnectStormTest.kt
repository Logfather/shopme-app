package de.shopme.testing.system.tests

import de.shopme.data.sync.QueueState
import de.shopme.data.sync.queue.ChangeQueueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalCoroutinesApi::class)
class ReconnectStormTest {

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun reconnectStorm_doesNotCreateDuplicateReplayOwners() = runTest {

        val runtime = FakeReconnectRuntime()

        repeat(50) {
            runtime.insertPendingChange()
        }

        val reconnectJob = launch {

            repeat(100) {

                runtime.goOffline()

                delay(5)

                runtime.goOnline()

                delay(5)
            }
        }

        val replayJobs = List(10) {

            launch {

                repeat(50) {

                    runtime.triggerSync()

                    delay(2)
                }
            }
        }

        reconnectJob.join()

        replayJobs.forEach {
            it.join()
        }

        advanceUntilIdle()

        assertFalse(
            runtime.hasDuplicateProcessingForSameId.get()
        )

        assertEquals(
            0,
            runtime.pendingCount()
        )
    }
}

private class FakeReconnectRuntime {

    private val mutex = Mutex()

    private val queue = mutableListOf<ChangeQueueEntity>()

    private val online = AtomicBoolean(true)

    val hasDuplicateProcessingForSameId = AtomicBoolean(false)

    /**
     * Tracks currently processing queue entry IDs.
     */
    private val processingIds =
        ConcurrentHashMap.newKeySet<String>()

    suspend fun insertPendingChange() {

        mutex.withLock {

            queue.add(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "ITEM",
                    entityId = UUID.randomUUID().toString(),
                    listId = "list-1",
                    operation = "UPDATE",
                    payload = "{}",
                    createdAt = System.currentTimeMillis(),
                    state = QueueState.PENDING.name,
                    retryCount = 0,
                    lastAttemptAt = null,
                    nextRetryAt = null,
                    progress = null,
                    errorMessage = null,
                    processingStartedAt = null,
                    baseVersion = 1L
                )
            )
        }
    }

    suspend fun goOffline() {
        online.set(false)
    }

    suspend fun goOnline() {
        online.set(true)
    }

    suspend fun triggerSync() {

        if (!online.get()) {
            return
        }

        while (true) {

            val claimed = claimNextPending()
                ?: break

            process(claimed)
        }
    }

    private suspend fun claimNextPending(): ChangeQueueEntity? {

        return mutex.withLock {

            val index = queue.indexOfFirst {
                it.state == QueueState.PENDING.name
            }

            if (index == -1) {
                return@withLock null
            }

            val current = queue[index]

            queue[index] = current.copy(
                state = QueueState.PROCESSING.name,
                processingStartedAt = System.currentTimeMillis()
            )

            current
        }
    }

    private suspend fun process(
        entity: ChangeQueueEntity
    ) {

        val inserted = processingIds.add(entity.id)

        if (!inserted) {
            hasDuplicateProcessingForSameId.set(true)
        }

        delay(1)

        mutex.withLock {

            val index = queue.indexOfFirst {
                it.id == entity.id
            }

            if (index != -1) {

                queue[index] = queue[index].copy(
                    state = QueueState.DONE.name
                )
            }
        }

        processingIds.remove(entity.id)
    }

    suspend fun pendingCount(): Int {

        return mutex.withLock {

            queue.count {
                it.state == QueueState.PENDING.name ||
                        it.state == QueueState.PROCESSING.name
            }
        }
    }
}