package de.shopme.testing.system.tests

import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.sync.RemoteApplyCoordinator
import de.shopme.data.sync.queue.ChangeQueueDao
import de.shopme.data.sync.queue.ChangeQueueEntity
import de.shopme.data.sync.queue.QueueStateCount
import de.shopme.data.sync.queue.SyncStateTuple
import de.shopme.data.sync.remote.RemoteApplyStateDao
import de.shopme.data.sync.remote.RemoteApplyStateEntity
import de.shopme.data.sync.telemetry.SyncTelemetryCollector
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(ExperimentalCoroutinesApi::class)
class RealtimeReplayIntegrationTest {

    private val dispatcher = StandardTestDispatcher()

    val telemetry = SyncTelemetryCollector()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun realtimeAndReplay_areSerializedPerEntity() = runTest {

        val itemDao = FakeItemDao()

        val queueDao = FakeChangeQueueDao()

        val remoteApplyStateDao =
            FakeRemoteApplyStateDao()


        val coordinator =
            RemoteApplyCoordinator(
                itemDao = itemDao,
                changeQueueDao = queueDao,
                remoteApplyStateDao = remoteApplyStateDao,
                telemetry = telemetry
            )

        val duplicateApplyDetected =
            AtomicBoolean(false)

        val entityId =
            UUID.randomUUID().toString()

        val replayItem =
            createItem(
                entityId = entityId,
                updatedAt = 100L
            )

        val realtimeItem =
            createItem(
                entityId = entityId,
                updatedAt = 200L
            )

        val jobs = List(100) { index ->

            async {

                val item =
                    if (index % 2 == 0) {
                        replayItem
                    } else {
                        realtimeItem
                    }

                val before =
                    itemDao.activeApplyCount()

                coordinator.applyRemoteItem(item)

                val after =
                    itemDao.activeApplyCount()

                if (before > 1 || after > 1) {
                    duplicateApplyDetected.set(true)
                }
            }
        }

        jobs.awaitAll()

        advanceUntilIdle()

        val final =
            itemDao.getById(entityId)

        assertFalse(
            duplicateApplyDetected.get()
        )

        assertEquals(
            200L,
            final?.updatedAt
        )
    }

    @Test
    fun staleRemote_isTrackedByTelemetry() = runTest {

        val itemDao = FakeItemDao()

        val queueDao = FakeChangeQueueDao()

        val remoteApplyStateDao =
            FakeRemoteApplyStateDao()

        val telemetry =
            SyncTelemetryCollector()

        val coordinator =
            RemoteApplyCoordinator(
                itemDao = itemDao,
                changeQueueDao = queueDao,
                remoteApplyStateDao = remoteApplyStateDao,
                telemetry = telemetry
            )

        val entityId =
            UUID.randomUUID().toString()

        val newer =
            createItem(
                entityId = entityId,
                updatedAt = 200L
            )

        val stale =
            createItem(
                entityId = entityId,
                updatedAt = 100L
            )

        coordinator.applyRemoteItem(newer)

        coordinator.applyRemoteItem(stale)

        advanceUntilIdle()

        val final =
            itemDao.getById(entityId)

        assertEquals(
            200L,
            final?.updatedAt
        )

        assertEquals(
            0,
            telemetry.metrics.value.remoteNewerApplied
        )

        assertTrue(
            telemetry.metrics.value.staleRemoteDiscards > 0
        )
    }

    private fun createItem(
        entityId: String,
        updatedAt: Long
    ): ShoppingItemEntity {

        return ShoppingItemEntity(
            id = entityId,
            listId = "list-1",
            name = "Milk",
            quantity = 1,
            category = "Food",
            isChecked = false,
            createdAt = 1L,
            updatedAt = updatedAt,
            deletedAt = null
        )
    }
}

private class FakeItemDao : ItemDao {

    private val mutex = Mutex()

    private val items =
        mutableMapOf<String, ShoppingItemEntity>()

    private var activeApplies = 0

    suspend fun activeApplyCount(): Int {

        return mutex.withLock {
            activeApplies
        }
    }

    override suspend fun upsert(
        item: ShoppingItemEntity
    ) {

        mutex.withLock {

            activeApplies++

            delay(2)

            items[item.id] = item

            activeApplies--
        }
    }

    override suspend fun getById(
        id: String
    ): ShoppingItemEntity? {

        return mutex.withLock {
            items[id]
        }
    }

    override suspend fun getItemById(
        itemId: String
    ): ShoppingItemEntity? {

        return getById(itemId)
    }

    override suspend fun getAll():
            List<ShoppingItemEntity> = emptyList()

    override fun observeItems():
            Flow<List<ShoppingItemEntity>> = emptyFlow()

    override fun observeItems(
        listId: String
    ): Flow<List<ShoppingItemEntity>> = emptyFlow()

    override suspend fun insertItems(
        items: List<ShoppingItemEntity>
    ) = Unit

    override suspend fun deleteItem(
        item: ShoppingItemEntity
    ) = Unit

    override fun observeActiveItems():
            Flow<List<ShoppingItemEntity>> = emptyFlow()

    override suspend fun clearAll() = Unit

    override fun observeItemsForList(
        listId: String
    ): Flow<List<ShoppingItemEntity>> = emptyFlow()

    override suspend fun getItemsForList(
        listId: String
    ): List<ShoppingItemEntity> = emptyList()

    override suspend fun insertAll(
        items: List<ShoppingItemEntity>
    ) = Unit

    override suspend fun deleteByListId(
        listId: String
    ) = Unit

    override suspend fun getPendingEntityIds(
        type: String
    ): List<String> = emptyList()

    override suspend fun getPendingItemIds():
            List<String> = emptyList()

    override suspend fun updateChecked(
        id: String,
        checked: Boolean,
        updatedAt: Long
    ) = Unit

    override suspend fun updateFullItem(
        id: String,
        name: String,
        quantity: Int,
        checked: Boolean,
        deletedAt: Long?,
        updatedAt: Long
    ) = Unit
}

private class FakeChangeQueueDao :
    ChangeQueueDao {

    override suspend fun getActiveByEntityId(
        entityId: String
    ): List<ChangeQueueEntity> {

        return emptyList()
    }

    override suspend fun insert(
        change: ChangeQueueEntity
    ) = Unit

    override suspend fun updateProgress(
        id: String,
        progress: Float
    ) = Unit

    override suspend fun updateProcessingState(
        id: String,
        state: String,
        timestamp: Long
    ) = Unit

    override suspend fun markProcessingIfPendingInternal(
        id: String,
        timestamp: Long
    ) = Unit

    override suspend fun updateRetry(
        id: String,
        state: String,
        retryCount: Int,
        timestamp: Long,
        nextRetryAt: Long
    ) = Unit

    override suspend fun resetRetryBackoff() = Unit

    override suspend fun updateState(
        id: String,
        state: String
    ) = Unit

    override suspend fun getState(
        id: String
    ): String? = null

    override suspend fun getPendingChanges(
        now: Long
    ): List<ChangeQueueEntity> = emptyList()

    override suspend fun getOldestPendingChange(
        now: Long
    ): ChangeQueueEntity? = null

    override suspend fun getOldestPendingChangeIgnoringRetry():
            ChangeQueueEntity? = null

    override suspend fun getPending(
        limit: Int,
        now: Long
    ): List<ChangeQueueEntity> = emptyList()

    override fun observeSyncStates():
            Flow<List<SyncStateTuple>> = emptyFlow()

    override fun observeQueueStats():
            Flow<List<QueueStateCount>> = emptyFlow()

    override suspend fun retryFailedChanges(
        entityId: String
    ) = Unit

    override suspend fun getLatestActiveByEntityId(
        entityId: String
    ): ChangeQueueEntity? = null

    override suspend fun getLatestPendingByEntityId(
        entityId: String
    ): ChangeQueueEntity? = null

    override suspend fun getPendingByEntityId(
        entityId: String
    ): List<ChangeQueueEntity> = emptyList()

    override suspend fun getPendingForEntity(
        entityId: String
    ): List<ChangeQueueEntity> = emptyList()

    override suspend fun deleteCompleted() = Unit

    override suspend fun deleteById(
        id: String
    ) = Unit

    override suspend fun deletePendingUpdatesForEntity(
        entityId: String
    ) = Unit

    override suspend fun clearAll() = Unit

    override suspend fun recoverInterruptedProcessing() = Unit

    override suspend fun markPendingByEntityId(
        itemId: String
    ) = Unit

    override suspend fun markDoneByEntityId(
        entityId: String
    ) = Unit

    override suspend fun getLatestChangeForItem(
        query: String
    ): ChangeQueueEntity? = null

    override suspend fun updateBaseVersionAndTimestamp(
        id: String,
        baseVersion: Long,
        createdAt: Long
    ) = Unit

    override suspend fun updateBaseVersion(
        id: String,
        baseVersion: Long,
        now: Long
    ) = Unit

    override suspend fun getAllChanges():
            List<ChangeQueueEntity> = emptyList()

    override suspend fun getChangeById(
        id: String
    ): ChangeQueueEntity? = null

    override suspend fun claimProcessingOwnership(
        id: String,
        timestamp: Long
    ): Int = 0
}

private class FakeRemoteApplyStateDao :
    RemoteApplyStateDao {

    private val states =
        mutableMapOf<String, RemoteApplyStateEntity>()

    override suspend fun getState(
        entityId: String
    ): RemoteApplyStateEntity? {

        return states[entityId]
    }

    override suspend fun upsert(
        entity: RemoteApplyStateEntity
    ) {

        states[entity.entityId] = entity
    }

    override suspend fun clearAll() {

        states.clear()
    }
}