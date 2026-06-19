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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalCoroutinesApi::class)
class RealtimeReplayRaceTest {

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
    fun realtimeAndReplay_doNotApplySameEntityConcurrently() = runTest {

        val runtime = FakeRealtimeReplayRuntime()

        repeat(200) {
            runtime.insertPendingChange()
        }

        val replayJob = launch {

            repeat(50) {

                runtime.runReplay()

                delay(2)
            }
        }

        val realtimeJob = launch {

            repeat(50) {

                runtime.simulateRealtimeApply()

                delay(2)
            }
        }

        replayJob.join()
        realtimeJob.join()

        advanceUntilIdle()

        assertFalse(
            runtime.hasConcurrentApplyForSameEntity.get()
        )

        assertFalse(
            runtime.hasResurrection.get()
        )
    }
}

private class FakeRealtimeReplayRuntime {

    private val mutex = Mutex()

    private val queue = mutableListOf<ChangeQueueEntity>()

    val hasConcurrentApplyForSameEntity =
        AtomicBoolean(false)

    val hasResurrection =
        AtomicBoolean(false)

    private val activeEntityIds =
        ConcurrentHashMap.newKeySet<String>()

    suspend fun insertPendingChange() {

        mutex.withLock {

            queue.add(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "ITEM",
                    entityId = UUID.randomUUID().toString(),
                    listId = "race-list",
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

    suspend fun runReplay() {

        val entity = mutex.withLock {

            queue.firstOrNull {
                it.state == QueueState.PENDING.name
            }?.also { found ->

                val index = queue.indexOf(found)

                queue[index] = found.copy(
                    state = QueueState.PROCESSING.name
                )
            }
        } ?: return

        applyEntity(entity.entityId)

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

        activeEntityIds.remove(entity.entityId)
    }

    suspend fun simulateRealtimeApply() {

        val entity = mutex.withLock {

            queue.randomOrNull()
        } ?: return

        applyEntity(entity.entityId)

        delay(1)

        activeEntityIds.remove(entity.entityId)
    }

    private suspend fun applyEntity(
        entityId: String
    ) {

        val inserted = activeEntityIds.add(entityId)

        if (!inserted) {
            hasConcurrentApplyForSameEntity.set(true)
        }

        delay(1)

        mutex.withLock {

            val entity = queue.firstOrNull {
                it.entityId == entityId
            }

            if (
                entity != null &&
                entity.state == QueueState.DONE.name
            ) {
                return@withLock
            }

            if (
                entity != null &&
                entity.state == QueueState.PENDING.name
            ) {
                hasResurrection.set(true)
            }
        }
    }
}