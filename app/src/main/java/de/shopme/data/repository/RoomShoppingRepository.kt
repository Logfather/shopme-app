package de.shopme.data.repository

import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.sync.toSyncStatus
import de.shopme.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.util.UUID

class RoomShoppingRepository(
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val changeQueueDao: ChangeQueueDao
) {

    // ============================================================
    // LISTS
    // ============================================================

    fun observeLists(): Flow<List<ShoppingListEntity>> {

        return listDao.observeLists()
    }

    suspend fun upsertLists(lists: List<ShoppingListEntity>) {
        listDao.insertLists(lists)
    }

    suspend fun deleteList(listId: String) {

        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "list",
                entityId = listId,
                listId = listId,
                operation = "DELETE",
                payload = null,
                createdAt = System.currentTimeMillis(),
                state = "PENDING",
                progress = 0f,
                baseVersion = 0
            )
        )

        listDao.deleteListById(listId)
    }

    suspend fun deleteAllLists() {

        val lists = listDao.observeLists().first()

        // 1. Queue DELETE für jede Liste
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
                    baseVersion = 0
                )
            )
        }

        // 2. Local löschen
        listDao.clearAll()
    }

    suspend fun createList(list: ShoppingListEntity) {

        // 1. Local speichern
        listDao.upsert(list)

        // 2. Queue Change
        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "list",
                entityId = list.id,
                listId = list.id,
                operation = "CREATE",
                payload = null,
                createdAt = System.currentTimeMillis(),
                state = "PENDING",
                progress = 0f,
                baseVersion = 0
            )
        )
    }

    // ============================================================
    // ITEMS
    // ============================================================

    fun observeItems(listId: String): Flow<List<ShoppingItemEntity>> {
        return itemDao.observeItemsForList(listId)
    }

    suspend fun addItem(item: ShoppingItemEntity) {

        // 1. LOCAL WRITE (optimistic)
        itemDao.upsert(item)

        // 2. QUEUE CHANGE
        enqueueChange(
            entityId = item.id,
            listId = item.listId,
            operation = "CREATE"
        )
    }

    suspend fun updateItem(item: ShoppingItemEntity) {

        itemDao.upsert(item)

        enqueueChange(
            entityId = item.id,
            listId = item.listId,
            operation = "UPDATE"
        )
    }

    suspend fun deleteItem(item: ShoppingItemEntity) {

        val deleted = item.copy(
            deletedAt = System.currentTimeMillis()
        )

        itemDao.upsert(deleted)

        enqueueChange(
            entityId = item.id,
            listId = item.listId,
            operation = "DELETE"
        )
    }

    // ============================================================
    // CHANGE QUEUE
    // ============================================================

    private suspend fun enqueueChange(
        entityId: String,
        listId: String,
        operation: String,
        baseVersion: Int = 0
    ) {
        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "item",
                entityId = entityId,
                listId = listId,
                operation = operation,
                payload = null,
                createdAt = System.currentTimeMillis(),
                state = "PENDING",
                progress = 0f,
                baseVersion = baseVersion
            )
        )
    }

    fun observeItemsWithSyncStatus(listId: String): Flow<List<Pair<ShoppingItemEntity, SyncStatus>>> {

        return combine(
            itemDao.observeItemsForList(listId),
            changeQueueDao.observeSyncStates()
        ) { items, syncStates ->

            val stateMap = syncStates
                .groupBy { it.entityId }

            items.map { item ->

                val states = stateMap[item.id]

                val status = when {

                    // ❌ höchste Priorität
                    states?.any { it.state == "FAILED" } == true -> {
                        SyncStatus.Failed()
                    }

                    // 🔄 irgendwas läuft
                    states?.any { it.state == "SYNCING" } == true -> {

                        val progress = states
                            .filter { it.state == "SYNCING" }
                            .mapNotNull { it.progress }
                            .average()
                            .toFloat()
                            .takeIf { !it.isNaN() }

                        SyncStatus.Syncing(progress = progress)
                    }

                    // 🕓 wartet
                    states?.any { it.state == "PENDING" } == true -> {
                        SyncStatus.Pending
                    }

                    // ✅ nichts offen
                    else -> {
                        SyncStatus.Synced
                    }
                }
                item to status
            }
        }
    }

    suspend fun retrySyncForItem(itemId: String) {
        changeQueueDao.retryFailedChanges(itemId)
    }

    suspend fun retryChange(change: ChangeQueueEntity) {
        changeQueueDao.updateRetry(
            id = change.id,
            state = "PENDING",
            retryCount = change.retryCount + 1,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun retryChangeByItemId(itemId: String) {
        val change = changeQueueDao.getLatestChangeForItem("%$itemId%")
            ?: return

        changeQueueDao.updateRetry(
            id = change.id,
            state = "PENDING",
            retryCount = change.retryCount + 1,
            timestamp = System.currentTimeMillis()
        )
    }
}