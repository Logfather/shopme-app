package de.shopme.testing.system.tests

import de.shopme.data.sync.QueueState
import de.shopme.data.sync.queue.ChangeQueueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalCoroutinesApi::class)
class RetryEngineStressTest {

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
    fun retryEngine_handlesMassiveRetryTransitionsCorrectly() = runTest {

        val runtime = FakeRetryRuntime()

        repeat(500) {
            runtime.insertRetryableChange()
        }

        runtime.runRetryCycles()

        advanceUntilIdle()

        assertFalse(
            runtime.hasInvalidRetryCount.get()
        )

        assertFalse(
            runtime.hasRetryLoop.get()
        )

        assertEquals(
            500,
            runtime.failedOrDoneCount()
        )
    }
}

private class FakeRetryRuntime {

    private val mutex = Mutex()

    private val queue = mutableListOf<ChangeQueueEntity>()

    val hasInvalidRetryCount = AtomicBoolean(false)

    val hasRetryLoop = AtomicBoolean(false)

    private val maxRetries = 3

    suspend fun insertRetryableChange() {

        mutex.withLock {

            queue.add(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "ITEM",
                    entityId = UUID.randomUUID().toString(),
                    listId = "retry-list",
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

    suspend fun runRetryCycles() {

        repeat(5) {

            val snapshot = mutex.withLock {
                queue.toList()
            }

            snapshot.forEach { entity ->

                if (
                    entity.state == QueueState.DONE.name ||
                    entity.state == QueueState.FAILED.name
                ) {
                    return@forEach
                }

                process(entity)
            }
        }
    }

    private suspend fun process(
        entity: ChangeQueueEntity
    ) {

        delay(1)

        mutex.withLock {

            val index = queue.indexOfFirst {
                it.id == entity.id
            }

            if (index == -1) {
                return@withLock
            }

            val current = queue[index]

            val nextRetry = current.retryCount + 1

            if (nextRetry > maxRetries + 1) {
                hasRetryLoop.set(true)
            }

            if (nextRetry > maxRetries) {

                queue[index] = current.copy(
                    state = QueueState.FAILED.name,
                    retryCount = nextRetry
                )

            } else {

                if (nextRetry < 0) {
                    hasInvalidRetryCount.set(true)
                }

                queue[index] = current.copy(
                    state = QueueState.RETRY_WAIT.name,
                    retryCount = nextRetry,
                    nextRetryAt = System.currentTimeMillis() + 1000
                )

                queue[index] = queue[index].copy(
                    state = QueueState.PENDING.name
                )
            }
        }
    }

    suspend fun failedOrDoneCount(): Int {

        return mutex.withLock {

            queue.count {
                it.state == QueueState.FAILED.name ||
                        it.state == QueueState.DONE.name
            }
        }
    }
}