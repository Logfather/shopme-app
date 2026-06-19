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
class ProcessDeathStressTest {

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
    fun processDeathRecovery_recoversInterruptedReplayWithoutDuplicates() = runTest {

        val runtime = FakeProcessDeathRuntime()

        repeat(250) {
            runtime.insertPendingChange()
        }

        val replayJob = launch {
            runtime.startReplay()
        }

        delay(50)

        runtime.simulateProcessDeath()

        replayJob.cancel()

        runtime.recoverInterruptedProcessing()

        runtime.startReplay()

        advanceUntilIdle()

        assertFalse(
            runtime.hasDuplicateProcessing.get()
        )

        assertFalse(
            runtime.hasStuckProcessingEntries.get()
        )

        assertEquals(
            250,
            runtime.doneCount()
        )

        assertEquals(
            0,
            runtime.pendingOrProcessingCount()
        )
    }
}

private class FakeProcessDeathRuntime {

    private val mutex = Mutex()

    private val queue = mutableListOf<ChangeQueueEntity>()

    val hasDuplicateProcessing = AtomicBoolean(false)

    val hasStuckProcessingEntries = AtomicBoolean(false)

    private val processingIds =
        ConcurrentHashMap.newKeySet<String>()

    suspend fun insertPendingChange() {

        mutex.withLock {

            queue.add(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "ITEM",
                    entityId = UUID.randomUUID().toString(),
                    listId = "process-death-list",
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

    suspend fun startReplay() {

        while (true) {

            val entity = claimNextPending()
                ?: break

            process(entity)
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
            hasDuplicateProcessing.set(true)
        }

        delay(5)

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

    suspend fun simulateProcessDeath() {

        processingIds.clear()

        mutex.withLock {

            queue.replaceAll {

                if (it.state == QueueState.PROCESSING.name) {

                    it.copy(
                        processingStartedAt = null
                    )

                } else {
                    it
                }
            }
        }
    }

    suspend fun recoverInterruptedProcessing() {

        mutex.withLock {

            queue.replaceAll {

                if (it.state == QueueState.PROCESSING.name) {

                    it.copy(
                        state = QueueState.PENDING.name,
                        processingStartedAt = null
                    )

                } else {
                    it
                }
            }

            val stuck = queue.any {
                it.state == QueueState.PROCESSING.name
            }

            if (stuck) {
                hasStuckProcessingEntries.set(true)
            }
        }
    }

    suspend fun doneCount(): Int {

        return mutex.withLock {

            queue.count {
                it.state == QueueState.DONE.name
            }
        }
    }

    suspend fun pendingOrProcessingCount(): Int {

        return mutex.withLock {

            queue.count {
                it.state == QueueState.PENDING.name ||
                        it.state == QueueState.PROCESSING.name
            }
        }
    }
}