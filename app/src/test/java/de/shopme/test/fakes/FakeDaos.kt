package de.shopme.test.fakes

import de.shopme.data.datasource.room.ItemDao
import de.shopme.data.datasource.room.ListDao
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.data.sync.SyncStateTuple
import de.shopme.domain.model.ShoppingListEntity
import de.shopme.domain.model.ShoppingItemEntity
import kotlinx.coroutines.flow.flowOf

class FakeListDao : ListDao {

    private val lists = mutableMapOf<String, ShoppingListEntity>()

    override suspend fun insertLists(lists: List<ShoppingListEntity>) {
        lists.forEach { this.lists[it.id] = it }
    }

    override suspend fun insert(list: ShoppingListEntity) {
        lists[list.id] = list
    }

    override suspend fun upsert(list: ShoppingListEntity) {
        lists[list.id] = list
    }

    override suspend fun deleteById(listId: String) {
        lists.remove(listId)
    }

    override suspend fun deleteListById(listId: String) {
        lists.remove(listId)
    }

    override suspend fun deleteList(list: ShoppingListEntity) {
        lists.remove(list.id)
    }

    override suspend fun clearAll() {
        lists.clear()
    }

    override suspend fun getListOnce(listId: String): ShoppingListEntity? {
        return lists[listId]
    }

    override suspend fun getListById(id: String): ShoppingListEntity? {
        return lists[id]
    }

    override suspend fun markDeleted(listId: String, timestamp: Long) {
        lists[listId]?.let {
            lists[listId] = it.copy(deletedAt = timestamp)
        }
    }

    override fun getAllListIdsSync(): List<String> {
        return lists.keys.toList()
    }

    override fun observeLists() = flowOf(emptyList<ShoppingListEntity>())

    // 👉 Helper
    suspend fun getById(id: String): ShoppingListEntity? = lists[id]

    override suspend fun deleteAllExcept(ids: List<String>) {
        lists.keys
            .filter { it !in ids }
            .forEach { lists.remove(it) }
    }
    override suspend fun observeListsOnce(): List<ShoppingListEntity> {
        return lists.values.filter { it.deletedAt == null }
    }

    override suspend fun getAllListsOnce(): List<ShoppingListEntity> {
        return lists.values.filter { it.deletedAt == null }
    }
}

class FakeItemDao : ItemDao {

    private val items = mutableMapOf<String, ShoppingItemEntity>()

    override suspend fun insertAll(items: List<ShoppingItemEntity>) {
        items.forEach { this.items[it.id] = it }
    }

    override suspend fun insertItems(items: List<ShoppingItemEntity>) {
        insertAll(items)
    }

    override suspend fun upsert(item: ShoppingItemEntity) {
        items[item.id] = item
    }

    override suspend fun deleteItem(item: ShoppingItemEntity) {
        items.remove(item.id)
    }

    override suspend fun deleteByListId(listId: String) {
        items.entries.removeIf { it.value.listId == listId }
    }

    override suspend fun clearAll() {
        items.clear()
    }

    override suspend fun getById(id: String): ShoppingItemEntity? {
        return items[id]
    }

    override suspend fun getItemsForList(listId: String): List<ShoppingItemEntity> {
        return items.values.filter { it.listId == listId }
    }

    override suspend fun getPendingEntityIds(type: String): List<String> = emptyList()

    override suspend fun getPendingItemIds(): List<String> = emptyList()

    override fun observeItems(): kotlinx.coroutines.flow.Flow<List<ShoppingItemEntity>> =
        flowOf(emptyList())

    override fun observeActiveItems(): kotlinx.coroutines.flow.Flow<List<ShoppingItemEntity>> =
        flowOf(emptyList())

    override fun observeItemsForList(listId: String): kotlinx.coroutines.flow.Flow<List<ShoppingItemEntity>> =
        flowOf(emptyList())
}

class FakeChangeQueueDao : ChangeQueueDao {

    val queue = mutableListOf<ChangeQueueEntity>()

    override suspend fun insert(change: ChangeQueueEntity) {
        queue.add(change)
    }

    override suspend fun getPending(limit: Int): List<ChangeQueueEntity> {
        return queue.filter { it.state == "PENDING" }.take(limit)
    }

    override suspend fun updateState(id: String, state: String) {
        queue.find { it.id == id }?.let {
            queue.remove(it)
            queue.add(it.copy(state = state))
        }
    }

    override suspend fun deleteCompleted() {
        queue.removeIf { it.state == "DONE" }
    }

    // --- nicht benötigte Methoden minimal stubben ---

    override suspend fun updateProgress(id: String, progress: Float) {}
    override suspend fun getPendingChanges(): List<ChangeQueueEntity> = queue
    override suspend fun updateRetry(id: String, state: String, retryCount: Int, timestamp: Long) {}
    override fun observeSyncStates() = flowOf(emptyList<SyncStateTuple>())
    override suspend fun retryFailedChanges(entityId: String) {}
    override suspend fun getLatestChangeForItem(query: String): ChangeQueueEntity? = null
    override suspend fun markSyncing(id: String, timestamp: Long) {}
    override suspend fun markSyncingIfPendingInternal(id: String, timestamp: Long) {}
    override suspend fun getState(id: String): String? = "PENDING"
    override suspend fun markPendingByEntityId(itemId: String) {
        // no-op für Tests
    }

    override suspend fun clearAll() {
        queue.clear()
    }
}