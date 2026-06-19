package de.shopme.testing.system.tests

import de.shopme.data.sync.QueueState
import de.shopme.data.sync.queue.ChangeQueueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
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
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class ReplayStressTest {

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
    fun replayStressTest_drainsLargeQueueWithoutDuplicateProcessing() = runTest {

        val runtime = FakeReplayStressRuntime()

        repeat(1_000) {
            runtime.insertPendingChange()
        }

        val jobs = List(16) {

            async {

                repeat(100) {

                    runtime.triggerReplay()

                    delay(1)
                }
            }
        }

        jobs.awaitAll()

        advanceUntilIdle()

        assertFalse(
            runtime.hasDuplicateProcessing.get()
        )

        assertFalse(
            runtime.hasReplayLoop.get()
        )

        assertEquals(
            0,
            runtime.pendingCount()
        )

        assertEquals(
            1_000,
            runtime.doneCount()
        )
    }
}

private class FakeReplayStressRuntime {

    private val mutex = Mutex()

    private val queue = mutableListOf<ChangeQueueEntity>()

    /**
     * Detects duplicate concurrent processing
     * of the same queue entry.
     */
    val hasDuplicateProcessing = AtomicBoolean(false)

    /**
     * Detects replay loop situations where
     * DONE entries become replayable again.
     */
    val hasReplayLoop = AtomicBoolean(false)

    /**
     * Tracks currently processing queue IDs.
     */
    private val processingIds =
        ConcurrentHashMap.newKeySet<String>()

    /**
     * Tracks completed queue IDs.
     */
    private val completedIds =
        ConcurrentHashMap.newKeySet<String>()

    /**
     * Throughput metric for debugging.
     */
    private val processedCount = AtomicInteger(0)

    suspend fun insertPendingChange() {

        mutex.withLock {

            queue.add(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "ITEM",
                    entityId = UUID.randomUUID().toString(),
                    listId = "stress-list",
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

    suspend fun triggerReplay() {

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

            /**
             * DONE entries must never
             * become replayable again.
             */
            if (completedIds.contains(current.id)) {
                hasReplayLoop.set(true)
            }

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

        /**
         * Simulated replay workload.
         */
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

        completedIds.add(entity.id)

        processingIds.remove(entity.id)

        processedCount.incrementAndGet()
    }

    suspend fun pendingCount(): Int {

        return mutex.withLock {

            queue.count {
                it.state == QueueState.PENDING.name ||
                        it.state == QueueState.PROCESSING.name
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
}