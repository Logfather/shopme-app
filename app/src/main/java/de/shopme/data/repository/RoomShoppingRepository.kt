package de.shopme.data.repository

import android.util.Log
import com.google.gson.Gson
import de.shopme.data.datasource.firestore.FirestoreDataSource
import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.mapper.EntityMapper.toDomain
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.domain.model.ListDeleteSnapshot
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.model.ShoppingItemEntity
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.SyncStatus
import kotlinx.coroutines.flow.*
import java.util.UUID

class RoomShoppingRepository(
    private val itemDao: ItemDao,
    private val listDao: ListDao,
    private val changeQueueDao: ChangeQueueDao,
    private val firestoreDataSource: FirestoreDataSource
){

    // ============================================================
    // LISTS
    // ============================================================

    fun observeLists(): Flow<List<ShoppingListEntity>> {
        return listDao.observeLists()
    }

    suspend fun upsertLists(lists: List<ShoppingListEntity>) {
        listDao.insertLists(lists)
    }

    fun observeAndStoreList(listId: String): Flow<Unit> {
        return firestoreDataSource
            .observeList(listId)
            .filterNotNull()
            .distinctUntilChanged()
            .map { entity ->
                listDao.insert(entity)
                Unit
            }
    }

    suspend fun getItemById(itemId: String): ShoppingItem? {
        return itemDao.getItemById(itemId)?.toDomain()
    }

    suspend fun deleteList(listId: String) {

        val now = System.currentTimeMillis()

        // 🔥 1. LOCAL DELETE
        listDao.deleteById(listId)
        itemDao.deleteByListId(listId)

        // 🔥 2. REMOTE DELETE (JETZT EINBAUEN)
        firestoreDataSource.softDeleteList(listId)

        // 🔥 3. OPTIONAL: Queue behalten (später für Offline relevant)
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
                    baseVersion = list.updatedAt   // ✅ FIX: keine /1000
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
                payload = Gson().toJson(list),   // 🔥 WICHTIG
                createdAt = now,
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
            .onEach { items ->
                items.forEach {
                    Log.d("DB_FLOW", "EMIT item=${it.id} checked=${it.isChecked}")
                }
            }
    }

    suspend fun addItem(item: ShoppingItemEntity) {

        itemDao.upsert(item)

    }

    suspend fun updateItem(item: ShoppingItemEntity) {

        val current = itemDao.getById(item.id)

        itemDao.updateFullItem(
            id = item.id,
            name = item.name,
            checked = item.isChecked,
            deletedAt = item.deletedAt,
            updatedAt = item.updatedAt
        )
    }

    suspend fun deleteItem(item: ShoppingItemEntity) {

        val current = itemDao.getById(item.id)

        val deleted = item.copy(
            deletedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        Log.d(
            "DB_CHECK",
            "DELETE item=${deleted.id} checked=${deleted.isChecked} deletedAt=${deleted.deletedAt}"
        )

        itemDao.upsert(deleted)
    }

    // ============================================================
    // CHANGE QUEUE
    // ============================================================
// TODO --> Entfernen
//    private suspend fun enqueueChange(
//        entityId: String,
//        listId: String,
//        operation: String,
//        baseVersion: Long
//    ) {
//
//        val existing = changeQueueDao.getActiveByEntityId(entityId)
//
//        Log.d("QUEUE_MERGE", "existing=${existing.map { it.operation }} id=$entityId")
//
//        val mergedOperation = when {
//            existing.any { it.operation == "CREATE" } && operation == "UPDATE" -> {
//                "CREATE" // CREATE bleibt dominant
//            }
//
//            existing.any { it.operation == "CREATE" } && operation == "DELETE" -> {
//                // CREATE + DELETE → komplett entfernen
//                changeQueueDao.deletePendingUpdatesForEntity(entityId)
//                Log.d("QUEUE_MERGE", "DROP CREATE+DELETE id=$entityId")
//                return
//            }
//
//            existing.any { it.operation == "UPDATE" } && operation == "UPDATE" -> {
//                "UPDATE" // nur ein UPDATE behalten
//            }
//
//            existing.any { it.operation == "UPDATE" } && operation == "DELETE" -> {
//                "DELETE" // DELETE gewinnt
//            }
//
//            else -> operation
//        }
//
//        // 🔥 alte Einträge entfernen (dedupe)
//        changeQueueDao.deletePendingUpdatesForEntity(entityId)
//
//        val newChange = ChangeQueueEntity(
//            id = UUID.randomUUID().toString(),
//            entityType = "item",
//            entityId = entityId,
//            listId = listId,
//            operation = mergedOperation,
//            payload = null,
//            createdAt = System.currentTimeMillis(),
//            state = "PENDING",
//            progress = 0f,
//            baseVersion = baseVersion
//        )
//
//        Log.d("QUEUE_MERGE", "ENQUEUE $mergedOperation id=$entityId")
//
//        changeQueueDao.insert(newChange)
//    }

    fun observeItemsWithSyncStatus(
        listId: String
    ): Flow<List<Pair<ShoppingItemEntity, SyncStatus>>> {

        val itemsFlow = itemDao.observeItemsForList(listId)

        val syncFlow = changeQueueDao.observeSyncStates()
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
                        "FAILED" -> SyncStatus.Failed()
                        "SYNCING" -> SyncStatus.Syncing(progress = latest.progress)
                        "PENDING" -> SyncStatus.Pending
                        else -> SyncStatus.Synced
                    }

                    item to status
                }
            }
            // 🔥 DAS ist der echte Fix:
            .distinctUntilChangedBy { list ->
                list.map { it.first.updatedAt to it.second }
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
                baseVersion = 0L   // ✅ FIX: neue Creation → kein Remote-Vergleich
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

    suspend fun addMembership(listId: String, userId: String) {

        val now = System.currentTimeMillis()

        // 🔥 Queue Event (v6)
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
    }
}