package de.shopme.domain.item

import android.util.Log
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.data.sync.ChangeQueueDao
import de.shopme.data.sync.ChangeQueueEntity
import de.shopme.domain.model.ShoppingItem
import de.shopme.data.mapper.EntityMapper.toEntity
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ItemActionHandler(
    private val roomRepository: RoomShoppingRepository,
    private val changeQueueDao: ChangeQueueDao,
    private val quantityMapper: QuantityMapper,
    private val categoryMapper: CategoryMapper
){

    private val itemLocks = mutableMapOf<String, Mutex>()

    private fun getLock(id: String): Mutex {
        return itemLocks.getOrPut(id) { Mutex() }
    }

    // ============================================================
    // ADD ITEM
    // ============================================================

    suspend fun addItem(
        name: String,
        listId: String
    ) {

        if (name.isBlank()) return

        val normalized = quantityMapper.normalize(name)
        val category = categoryMapper.resolve(normalized)

        val now = System.currentTimeMillis()

        val item = ShoppingItem(
            id = UUID.randomUUID().toString(),
            listId = listId,
            name = normalized,
            quantity = 1,
            category = category,
            isChecked = true,
            deletedAt = null,
            createdAt = now,
            updatedAt = now
        )

        val entity = item.toEntity()

        val existing = changeQueueDao.getPendingByEntityId(entity.id)

        if (existing.any { it.operation == "CREATE" }) {
            Log.d("QUEUE_SKIP", "Skip duplicate CREATE for ${entity.id}")
            return
        }

        //Log.d("ITEM_HANDLER", "addItem NEW id=${entity.id}")
        Log.d("ITEM_HANDLER", "addItem")

        roomRepository.addItem(entity)
    }

    // ============================================================
    // UPDATE ITEM CHECKED
    // ============================================================

    suspend fun updateItemChecked(
        itemId: String,
        newChecked: Boolean
    ) {
        val lock = getLock(itemId)
        Log.d("ITEM_HANDLER", "updateItemChecked")

        lock.withLock {

            val current = roomRepository.getItemById(itemId)
                ?: return

            val now = System.currentTimeMillis()

            if (current.isChecked == newChecked) {
                return
            }

            val updated = current.copy(
                isChecked = newChecked,
                updatedAt = now
            )

            val entity = updated.toEntity()

            Log.d("DB_CHECK", "UPDATE item=${entity.id} checked=${entity.isChecked}")

            roomRepository.updateItem(entity)

            changeQueueDao.deletePendingUpdatesForEntity(entity.id)

        }
    }

    // ============================================================
    // UPDATE ITEM (NAME)
    // ============================================================

    suspend fun updateItem(
        item: ShoppingItem,
        newName: String
    ) {
        val lock = getLock(item.id)

        lock.withLock {

            val now = System.currentTimeMillis()

            Log.d("ITEM_HANDLER", "updateItem")

            // 🔥 WICHTIG: NICHT current verwenden!
            val updated = item.copy(
                name = newName,
                updatedAt = now
            )

            val entity = updated.toEntity()

            Log.d(
                "DB_CHECK",
                "UPDATE item=${entity.id} checked=${entity.isChecked}"
            )

            roomRepository.updateItem(entity)

            changeQueueDao.deletePendingUpdatesForEntity(entity.id)

            enqueue(
                entityId = entity.id,
                listId = entity.listId,
                operation = "UPDATE",
                createdAt = now,
                baseVersion = item.updatedAt
            )
        }
    }

    // ============================================================
    // DELETE ITEM
    // ============================================================

    suspend fun deleteItem(item: ShoppingItem) {

        val lock = getLock(item.id)

        lock.withLock {
            val now = System.currentTimeMillis()

            val updated = item.copy(
                deletedAt = now,
                updatedAt = now
            )

            val entity = updated.toEntity()

            Log.d(
                "DB_CHECK",
                "DELETE item=${entity.id} checked=${entity.isChecked} deletedAt=${entity.deletedAt}"
            )

            Log.d("ITEM_HANDLER", "deleteItem")

            roomRepository.deleteItem(entity)

            changeQueueDao.deletePendingUpdatesForEntity(entity.id)
        }
    }
    private suspend fun enqueue(
        entityId: String,
        listId: String,
        operation: String,
        createdAt: Long,
        baseVersion: Long
    ) {
        // 🔍 Prüfe bestehende Queue Einträge (Deduplication Vorbereitung)
        val existing = changeQueueDao
            .getPending(limit = 1000)
            .firstOrNull {
                it.entityId == entityId &&
                        it.state != "DONE"
            }

        if (existing != null) {
            Log.d(
                "QUEUE_DEDUP",
                "Replace existing op=${existing.operation} with op=$operation id=$entityId"
            )

            // 🔥 Alte Operation entfernen (z. B. UPDATE → DELETE)
            changeQueueDao.deleteById(existing.id)
        }

        val entity = ChangeQueueEntity(
            id = java.util.UUID.randomUUID().toString(),
            entityId = entityId,
            listId = listId,
            entityType = "item",
            operation = operation,
            payload = null, // 👈 WICHTIG (oder "" je nach Typ!)
            state = "PENDING",
            createdAt = createdAt,
            baseVersion = baseVersion
        )

        Log.d(
            "QUEUE_ENQUEUE",
            "ADD op=$operation id=$entityId base=$baseVersion"
        )

        changeQueueDao.insert(entity)
    }
}