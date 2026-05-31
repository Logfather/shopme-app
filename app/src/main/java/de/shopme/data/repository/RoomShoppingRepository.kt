package de.shopme.data.repository

import com.google.gson.Gson
import de.shopme.data.datasource.firestore.FirestoreGateway
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.mapper.EntityMapper.toDomain
import de.shopme.data.sync.queue.ChangeQueueDao
import de.shopme.data.sync.queue.ChangeQueueEntity
import de.shopme.data.sync.SyncCoordinator
import de.shopme.data.sync.queue.SyncStateTuple
import de.shopme.data.sync.logging.RecoveryLog
import de.shopme.data.sync.logging.SyncLog
import de.shopme.domain.life.LifeEvent
import de.shopme.domain.life.NimelisEventBus
import de.shopme.domain.model.ListDeleteSnapshot
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.SyncOverview
import de.shopme.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import java.util.UUID

class RoomShoppingRepository(
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val changeQueueDao: ChangeQueueDao,
    private val firestoreDataSource: FirestoreGateway,
    private var syncCoordinator: SyncCoordinator? = null,
    private val nimelisEventBus: NimelisEventBus
) {

    // ============================================================
    // LISTS
    // ============================================================

    private val lastWriteAt = mutableMapOf<String, Long>()

    private val lastUpdateMap = mutableMapOf<String, Long>()

    fun attachSyncCoordinator(
        syncCoordinator: SyncCoordinator
    ) {
        this.syncCoordinator = syncCoordinator

        SyncLog.orchestrator(
            "[Attach] SyncCoordinator attached to RoomShoppingRepository"
        )
    }

    fun observeLists(): Flow<List<ShoppingListEntity>> {
        return listDao.observeLists()
    }

    suspend fun upsertLists(lists: List<ShoppingListEntity>) {
        listDao.upsertLists(lists)
    }

    fun observeAndStoreList(listId: String): Flow<Unit> {
        return firestoreDataSource
            .observeListById(listId)
            .filterNotNull()
            .distinctUntilChanged()
            .map { entity ->
                listDao.insert(entity)
                Unit
            }
    }

    suspend fun softDeleteItem(
        itemId: String
    ) {

        val current =
            itemDao.getById(itemId)
                ?: return

        val now =
            System.currentTimeMillis()

        itemDao.updateFullItem(
            id = current.id,
            name = current.name,
            quantity = current.quantity,
            checked = current.isChecked,
            deletedAt = now,
            updatedAt = now
        )

        enqueue(
            entityId = current.id,
            listId = current.listId,
            operation = "DELETE",
            baseVersion = current.updatedAt
        )
    }

    suspend fun getItemById(itemId: String): ShoppingItem? {
        return itemDao.getItemById(itemId)?.toDomain()
    }

    suspend fun deleteList(listId: String) {

        val now = System.currentTimeMillis()

        listDao.deleteById(listId)
        itemDao.deleteByListId(listId)

        firestoreDataSource.softDeleteList(listId)

        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "list",
                entityId = listId,
                listId = listId,
                operation = "DELETE",
                payload = null,
                createdAt = now,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )
    }

    suspend fun deleteAllLists() {

        val lists = listDao.observeLists().first()

        lists.forEach { list ->
            changeQueueDao.insert(
                ChangeQueueEntity(
                    id = UUID.randomUUID().toString(),
                    entityType = "list",
                    entityId = list.id,
                    listId = list.id,
                    operation = "DELETE",
                    payload = null,
                    createdAt = System.currentTimeMillis(),
                    state = "PENDING",
                    progress = 0f,
                    baseVersion = list.updatedAt
                )
            )
        }

        lists.forEach { list ->
            listDao.markDeleted(list.id, System.currentTimeMillis())
        }
    }

    suspend fun createList(list: ShoppingListEntity) {

        val now = System.currentTimeMillis()

        listDao.insert(list)

        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "list",
                entityId = list.id,
                listId = list.id,
                operation = "CREATE",
                payload = Gson().toJson(list),
                createdAt = now,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        SyncLog.orchestrator(
            "[Trigger] Sync after CREATE LIST"
        )

        syncCoordinator?.triggerSync()
    }

    // ============================================================
    // ITEMS
    // ============================================================

    fun observeItems(listId: String): Flow<List<ShoppingItemEntity>> {
        return itemDao.observeItemsForList(listId)
            .map { items ->

                items
                    .groupBy { it.id }
                    .map { (_, list) ->
                        list
                            .sortedWith(
                                compareByDescending<ShoppingItemEntity> { it.updatedAt }
                                    .thenByDescending { it.createdAt }
                            )
                            .first()
                    }
            }
            .onEach { items ->
                items.forEach {
                    // optional debug
                }
            }
    }

    suspend fun createItem(item: ShoppingItemEntity) {

        try {

            SyncLog.queue(
                "[Create] entered item=${item.name}"
            )

            SyncLog.queue(
                "[Upsert] before item=${item.id}"
            )

            itemDao.upsert(item)

            SyncLog.queue(
                "[Upsert] success item=${item.id}"
            )

            SyncLog.queue(
                "[Enqueue] before item=${item.id}"
            )

            enqueue(
                entityId = item.id,
                listId = item.listId,
                operation = "CREATE",
                baseVersion = 0L
            )

            SyncLog.queue(
                "[Enqueue] finished item=${item.id}"
            )

            nimelisEventBus.emit(
                LifeEvent.ItemAdded(
                    itemId = item.id,
                    listId = item.listId,
                    itemName = item.name
                )
            )

        } catch (e: Exception) {

            RecoveryLog.processError(
                "createItem failed",
                e
            )

            throw e
        }
    }

    suspend fun updateItem(item: ShoppingItemEntity) {

        val current = itemDao.getById(item.id)

        if (
            current != null &&
            current.name == item.name &&
            current.quantity == item.quantity &&
            current.isChecked == item.isChecked &&
            current.deletedAt == item.deletedAt
        ) {
            return
        }

        val now = System.currentTimeMillis()

        itemDao.updateFullItem(
            id = item.id,
            name = item.name,
            quantity = item.quantity,
            checked = item.isChecked,
            deletedAt = item.deletedAt,
            updatedAt = now
        )

        enqueue(
            entityId = item.id,
            listId = item.listId,
            operation = "UPDATE",
            baseVersion = current?.updatedAt ?: 0L
        )
    }

    suspend fun deleteItem(item: ShoppingItemEntity) {

        val current = itemDao.getById(item.id)

        val now = System.currentTimeMillis()

        val deleted = item.copy(
            deletedAt = now,
            updatedAt = now
        )

        itemDao.upsert(deleted)

        enqueue(
            entityId = item.id,
            listId = item.listId,
            operation = "DELETE",
            baseVersion = current?.updatedAt ?: 0L
        )
    }

    // ============================================================
    // CHANGE QUEUE
    // ============================================================

    fun observeItemsWithSyncStatus(
        listId: String
    ): Flow<List<Pair<ShoppingItemEntity, SyncStatus>>> {

        val itemsFlow = itemDao.observeItemsForList(listId)

        val syncFlow: Flow<Map<String, SyncStateTuple?>> =
            changeQueueDao.observeSyncStates()
                .map { syncStates ->
                    syncStates
                        .groupBy { it.entityId }
                        .mapValues { (_, states) ->
                            states.maxByOrNull { it.createdAt }
                        }
                }

        return itemsFlow
            .combine(syncFlow) { items, latestStateMap ->

                items.map { item ->

                    val latest = latestStateMap[item.id]

                    val status = when (latest?.state) {
                        "FAILED" -> SyncStatus.Failed(
                            retryCount = latest.retryCount
                        )

                        "SYNCING" -> SyncStatus.Syncing(
                            progress = latest.progress ?: 0f
                        )

                        "PENDING" -> SyncStatus.Pending

                        else -> SyncStatus.Synced
                    }

                    item to status
                }
            }
            .distinctUntilChangedBy { list ->
                list.map { it.first.updatedAt to it.second }
            }
    }

    suspend fun retrySyncForItem(itemId: String) {
        changeQueueDao.retryFailedChanges(itemId)
    }

    suspend fun retryChange(change: ChangeQueueEntity) {

        val now =
            System.currentTimeMillis()

        val newRetry =
            change.retryCount + 1

        val retryDelayMs =
            minOf(
                newRetry * 5000L,
                60000L
            )

        val nextRetryAt =
            now + retryDelayMs

        changeQueueDao.updateRetry(
            id = change.id,
            state = "PENDING",
            retryCount = newRetry,
            timestamp = now,
            nextRetryAt = nextRetryAt
        )
    }

    suspend fun retryChangeByItemId(itemId: String) {

        val change =
            changeQueueDao.getLatestChangeForItem("%$itemId%")
                ?: return

        val now =
            System.currentTimeMillis()

        val newRetry =
            change.retryCount + 1

        val retryDelayMs =
            minOf(
                newRetry * 5000L,
                60000L
            )

        val nextRetryAt =
            now + retryDelayMs

        changeQueueDao.updateRetry(
            id = change.id,
            state = "PENDING",
            retryCount = newRetry,
            timestamp = now,
            nextRetryAt = nextRetryAt
        )
    }

    suspend fun markListDeleted(listId: String) {
        listDao.markDeleted(listId, System.currentTimeMillis())
    }

    suspend fun createListDeleteSnapshot(
        listId: String
    ): ListDeleteSnapshot {

        val list =
            listDao.getListById(listId)
                ?: throw IllegalStateException(
                    "List not found for snapshot: $listId"
                )

        val items = itemDao.getItemsForList(listId)

        return ListDeleteSnapshot(
            list = list,
            items = items,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun restoreList(snapshot: ListDeleteSnapshot) {

        val now = System.currentTimeMillis()

        val updatedAt = maxOf(
            snapshot.list.updatedAt,
            now
        )

        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "list",
                entityId = snapshot.list.id,
                listId = snapshot.list.id,
                operation = "CREATE",
                payload = null,
                createdAt = now,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        listDao.upsert(
            snapshot.list.copy(
                deletedAt = null,
                updatedAt = updatedAt
            )
        )

        itemDao.insertAll(
            snapshot.items.map {
                it.copy(
                    deletedAt = null,
                    updatedAt = maxOf(it.updatedAt, now)
                )
            }
        )
    }

    suspend fun addMembership(
        listId: String,
        userId: String
    ) {

        val now = System.currentTimeMillis()

        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "membership",
                entityId = "${userId}_$listId",
                listId = listId,
                operation = "ADD",
                payload = userId,
                createdAt = now,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        syncCoordinator?.triggerSync()
    }

    suspend fun consumeInvite(inviteId: String) {

        val now = System.currentTimeMillis()

        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "invite",
                entityId = inviteId,
                listId = "",
                operation = "CONSUME",
                payload = inviteId,
                createdAt = now,
                state = "PENDING",
                progress = 0f,
                baseVersion = 0L
            )
        )

        syncCoordinator?.triggerSync()
    }

    private suspend fun enqueue(
        entityId: String,
        listId: String,
        operation: String,
        baseVersion: Long
    ) {
        SyncLog.queue(
            "[Guard] enqueue operation=$operation"
        )

        val lastSynced = itemDao.getById(entityId)

        if (
            lastSynced != null &&
            operation == "UPDATE"
        ) {

            val now = System.currentTimeMillis()

            val lastRemoteWrite = lastWriteAt[entityId]

            if (
                lastRemoteWrite != null &&
                now - lastRemoteWrite < 500
            ) {

                SyncLog.orchestrator(
                    "[Guard] BURST RETURN operation=$operation"
                )

                return
            }
        }

        val now = System.currentTimeMillis()

        val last = lastWriteAt[entityId]

        if (
            operation == "UPDATE" &&
            last != null &&
            now - last < 500
        ) {

            SyncLog.orchestrator(
                "[Debounce] RETURN operation=$operation"
            )

            return
        }

        val existing =
            changeQueueDao.getLatestPendingByEntityId(entityId)

        if (existing != null) {

            when {

                operation == "DELETE" -> {
                    changeQueueDao.deleteById(existing.id)
                }

                existing.operation == "CREATE" &&
                        operation == "UPDATE" -> {

                    return
                }

                existing.operation == "CREATE" &&
                        operation == "DELETE" -> {

                    changeQueueDao.deleteById(existing.id)
                    return
                }

                existing.operation == "UPDATE" &&
                        operation == "UPDATE" -> {

                    changeQueueDao.updateBaseVersion(
                        id = existing.id,
                        baseVersion = baseVersion
                    )

                    return
                }

                else -> {
                    changeQueueDao.deleteById(existing.id)
                }
            }
        }

        val entity = ChangeQueueEntity(
            id = UUID.randomUUID().toString(),
            entityId = entityId,
            listId = listId,
            entityType = "item",
            operation = operation,
            payload = null,
            state = "PENDING",
            createdAt = System.currentTimeMillis(),
            baseVersion = baseVersion
        )

        SyncLog.queue(
            "[Insert] op=$operation entity=$entityId state=${entity.state}"
        )

        changeQueueDao.insert(entity)

        val afterInsert = changeQueueDao.getAllChanges()

        SyncLog.queue(
            "[Metric] size=${afterInsert.size}"
        )

        SyncLog.orchestrator(
            "[Trigger] enqueue op=$operation entity=$entityId"
        )

        syncCoordinator?.triggerSync()
    }

    fun observeSyncOverview(): Flow<SyncOverview> {

        return changeQueueDao.observeQueueStats()
            .map { stats ->

                var pending = 0
                var syncing = 0
                var failed = 0

                stats.forEach {

                    when (it.state) {

                        "PENDING" -> pending = it.count

                        "SYNCING" -> syncing = it.count

                        "FAILED" -> failed = it.count
                    }
                }

                SyncOverview(
                    pending = pending,
                    syncing = syncing,
                    failed = failed
                )
            }
    }

    fun onSyncWriteSuccess(
        entityId: String,
        timestamp: Long
    ) {
        lastWriteAt[entityId] = timestamp
    }
}