package de.shopme.testing.system.tests

import de.shopme.data.sync.QueueState
import de.shopme.data.sync.queue.ChangeQueueEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
class ReplayOwnershipTest {

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
    fun parallelOwnershipClaim_onlyOneOwnerWins() = runBlocking {

        val dao = FakeOwnershipDao()

        val entity = createPendingChange()

        dao.insert(entity)

        val concurrentOwners = AtomicInteger(0)

        val maxConcurrentOwners = AtomicInteger(0)

        val jobs = List(3) {

            async {

                val claimed = dao.claimProcessingOwnership(
                    id = entity.id,
                    processingStartedAt = System.currentTimeMillis()
                )

                if (claimed == 1) {

                    val current = concurrentOwners.incrementAndGet()

                    maxConcurrentOwners.updateAndGet { old ->
                        maxOf(old, current)
                    }

                    delay(100)

                    dao.updateState(
                        id = entity.id,
                        state = QueueState.DONE.name
                    )

                    concurrentOwners.decrementAndGet()
                }
            }
        }

        jobs.awaitAll()

        assertEquals(
            1,
            maxConcurrentOwners.get()
        )
    }

    private fun createPendingChange(): ChangeQueueEntity {

        return ChangeQueueEntity(
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
    }
}

private class FakeOwnershipDao {

    private val mutex = Mutex()

    private val queue = mutableListOf<ChangeQueueEntity>()

    suspend fun insert(
        entity: ChangeQueueEntity
    ) {

        mutex.withLock {
            queue.add(entity)
        }
    }

    suspend fun claimProcessingOwnership(
        id: String,
        processingStartedAt: Long
    ): Int {

        return mutex.withLock {

            val index = queue.indexOfFirst {
                it.id == id
            }

            if (index == -1) {
                return@withLock 0
            }

            val current = queue[index]

            if (current.state != QueueState.PENDING.name) {
                return@withLock 0
            }

            queue[index] = current.copy(
                state = QueueState.PROCESSING.name,
                processingStartedAt = processingStartedAt
            )

            1
        }
    }

    suspend fun updateState(
        id: String,
        state: String
    ) {

        mutex.withLock {

            val index = queue.indexOfFirst {
                it.id == id
            }

            if (index == -1) {
                return@withLock
            }

            queue[index] = queue[index].copy(
                state = state
            )
        }
    }
}