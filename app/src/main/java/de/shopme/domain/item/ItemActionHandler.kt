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

            Log.d(
                "DB_CHECK",
                "UPDATE item=${entity.id} checked=${entity.isChecked}"
            )

            // ✅ KORREKT
            roomRepository.updateItem(entity)

            //changeQueueDao.deletePendingUpdatesForEntity(entity.id)

            enqueue(
                entityId = entity.id,
                listId = entity.listId,
                operation = "UPDATE",
                createdAt = now,
                baseVersion = current.updatedAt
            )
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

            // 🔥 HOL DIR DEN AKTUELLEN STATE AUS DER DB
            val current = roomRepository.getItemById(item.id)
                ?: return

            val updated = current.copy(
                name = newName,
                isChecked = true,   // 👈 IMMER setzen beim Edit
                updatedAt = now
            )

            val entity = updated.toEntity()

            Log.d(
                "DB_CHECK",
                "UPDATE item=${entity.id} checked=${entity.isChecked}"
            )

            roomRepository.updateItem(entity)

            //changeQueueDao.deletePendingUpdatesForEntity(entity.id)

            enqueue(
                entityId = entity.id,
                listId = entity.listId,
                operation = "UPDATE",
                createdAt = now,
                baseVersion = current.updatedAt   // 👈 wichtig: current!
            )
        }
    }

    // ============================================================
    // DELETE ITEM
    // ============================================================

    suspend fun deleteItem(item: ShoppingItem) {

        val lock = getLock(item.id)

        lock.withLock {

            val current = roomRepository.getItemById(item.id)

            // 🔥 FIX: Wenn bereits gelöscht → sofort raus
            if (current?.deletedAt != null) {
                Log.d("ITEM_HANDLER", "deleteItem SKIP already deleted id=${item.id}")
                return
            }

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

            //changeQueueDao.deletePendingUpdatesForEntity(entity.id)
        }
    }
    private suspend fun enqueue(
        entityId: String,
        listId: String,
        operation: String,
        createdAt: Long,
        baseVersion: Long
    ) {

        val existing = changeQueueDao.getLatestPendingByEntityId(entityId)

        if (existing != null) {

            Log.d(
                "QUEUE_DEDUP",
                "existing=${existing.operation} new=$operation id=$entityId"
            )

            when {

                // --------------------------------------------------
                // CREATE + UPDATE → KEEP CREATE
                // --------------------------------------------------
                existing.operation == "CREATE" && operation == "UPDATE" -> {
                    Log.d("QUEUE_DEDUP", "KEEP CREATE, skip UPDATE id=$entityId")
                    return
                }

                // --------------------------------------------------
                // CREATE + DELETE → REMOVE BOTH (no-op)
                // --------------------------------------------------
                existing.operation == "CREATE" && operation == "DELETE" -> {
                    Log.d("QUEUE_DEDUP", "DROP CREATE+DELETE id=$entityId")
                    changeQueueDao.deleteById(existing.id)
                    return
                }

                // --------------------------------------------------
                // UPDATE + UPDATE → REPLACE
                // --------------------------------------------------
                existing.operation == "UPDATE" && operation == "UPDATE" -> {
                    changeQueueDao.deleteById(existing.id)
                }

                // --------------------------------------------------
                // UPDATE + DELETE → DELETE gewinnt
                // --------------------------------------------------
                existing.operation == "UPDATE" && operation == "DELETE" -> {
                    changeQueueDao.deleteById(existing.id)
                }

                // --------------------------------------------------
                // DELETE + UPDATE → UPDATE (Undo Verhalten)
                // --------------------------------------------------
                existing.operation == "DELETE" && operation == "UPDATE" -> {
                    changeQueueDao.deleteById(existing.id)
                }

                // --------------------------------------------------
                // Default: ersetzen
                // --------------------------------------------------
                else -> {
                    changeQueueDao.deleteById(existing.id)
                }
            }
        }

        val entity = ChangeQueueEntity(
            id = java.util.UUID.randomUUID().toString(),
            entityType = "item",
            entityId = entityId,
            listId = listId,
            operation = operation,
            payload = null,
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