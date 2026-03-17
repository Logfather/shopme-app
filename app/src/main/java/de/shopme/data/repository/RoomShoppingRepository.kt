package de.shopme.data.repository

import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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

        listDao.deleteListById(listId)
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
            operation = "CREATE"
        )
    }

    suspend fun updateItem(item: ShoppingItemEntity) {

        itemDao.upsert(item)

        enqueueChange(
            entityId = item.id,
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
            operation = "DELETE"
        )
    }

    // ============================================================
    // CHANGE QUEUE
    // ============================================================

    private suspend fun enqueueChange(
        entityId: String,
        operation: String
    ) {

        changeQueueDao.insert(
            ChangeQueueEntity(
                id = UUID.randomUUID().toString(),
                entityType = "item",
                entityId = entityId,
                operation = operation,
                payload = null,
                createdAt = System.currentTimeMillis(),
                state = "PENDING"
            )
        )
    }

    fun observeItemsWithSyncStatus(listId: String): Flow<List<Pair<ShoppingItemEntity, SyncStatus>>> {

        return combine(
            itemDao.observeItemsForList(listId),
            changeQueueDao.observeSyncStates()
        ) { items, syncStates ->

            val stateMap = syncStates.associate { it.entityId to it.state }

            items.map { item ->

                val status = when (stateMap[item.id]) {
                    "PENDING" -> SyncStatus.PENDING
                    "SYNCING" -> SyncStatus.SYNCING
                    "FAILED" -> SyncStatus.FAILED
                    else -> SyncStatus.SYNCED
                }

                item to status
            }
        }
    }
}