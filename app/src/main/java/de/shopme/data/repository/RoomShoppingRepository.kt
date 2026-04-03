package de.shopme.data.repository

import android.util.Log
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.domain.model.ListDeleteSnapshot
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

        val current = listDao.getListById(listId)

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
                baseVersion = (current?.updatedAt ?: 0)
            )
        )

        listDao.markDeleted(listId, System.currentTimeMillis())
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
                    baseVersion = (list.updatedAt / 1000)
                )
            )
        }

        lists.forEach { list ->
            listDao.markDeleted(list.id, System.currentTimeMillis())
        }
    }

    suspend fun createList(list: ShoppingListEntity) {

        Log.d("CREATE_DEBUG", "Repository.createList CALLED for ${list.id}")

        listDao.upsert(list)

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
                baseVersion = 0L
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

        itemDao.upsert(item)

        enqueueChange(
            entityId = item.id,
            listId = item.listId,
            operation = "CREATE",
            baseVersion = 0L
        )
    }

    suspend fun updateItem(item: ShoppingItemEntity) {

        val current = itemDao.getById(item.id)

        itemDao.upsert(item)

        enqueueChange(
            entityId = item.id,
            listId = item.listId,
            operation = "UPDATE",
            baseVersion = current?.updatedAt ?: 0L
        )
    }

    suspend fun deleteItem(item: ShoppingItemEntity) {

        val current = itemDao.getById(item.id)

        val deleted = item.copy(
            deletedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        itemDao.upsert(deleted)

        enqueueChange(
            entityId = item.id,
            listId = item.listId,
            operation = "DELETE",
            baseVersion = current?.updatedAt ?: 0L
        )
    }

    // ============================================================
    // CHANGE QUEUE
    // ============================================================

    private suspend fun enqueueChange(
        entityId: String,
        listId: String,
        operation: String,
        baseVersion: Long = 0L
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
                baseVersion = baseVersion // ⚠️ wegen aktuellem Schema
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


    suspend fun markListDeleted(listId: String) {
        listDao.markDeleted(listId, System.currentTimeMillis())
    }


    suspend fun createListDeleteSnapshot(listId: String): ListDeleteSnapshot {

        val list = listDao.getListById(listId)
            ?: throw IllegalStateException("List not found for snapshot: $listId")

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



        // 1. Queue
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
                baseVersion = (snapshot.list.updatedAt / 1000)
            )
        )

        // 2. List
        listDao.upsert(
            snapshot.list.copy(
                deletedAt = null,
                updatedAt = updatedAt
            )
        )

        // 3. Items
        itemDao.insertAll(
            snapshot.items.map {
                it.copy(
                    deletedAt = null,
                    updatedAt = maxOf(it.updatedAt, now)
                )
            }
        )
    }
}