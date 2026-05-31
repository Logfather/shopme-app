package de.shopme.domain.item

import de.shopme.data.mapper.EntityMapper.toEntity
import de.shopme.data.repository.RoomShoppingRepository
import de.shopme.domain.model.ShoppingItem
import de.shopme.domain.service.CategoryMapper
import de.shopme.domain.service.QuantityMapper
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

class ItemActionHandler(
    private val roomRepository: RoomShoppingRepository,
    private val quantityMapper: QuantityMapper,
    private val categoryMapper: CategoryMapper
) {

    private val itemLocks =
        mutableMapOf<String, Mutex>()

    private fun getLock(
        id: String
    ): Mutex {

        return itemLocks.getOrPut(id) {
            Mutex()
        }
    }

    // ============================================================
    // ADD ITEM
    // ============================================================

    suspend fun addItem(
        name: String,
        listId: String
    ) {

        if (name.isBlank()) {
            return
        }

        val normalized =
            quantityMapper.normalize(name)

        val category =
            categoryMapper.resolve(normalized)

        val now =
            System.currentTimeMillis()

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

        val entity =
            item.toEntity()

        roomRepository.createItem(entity)
    }

    // ============================================================
    // UPDATE ITEM CHECKED
    // ============================================================

    suspend fun updateItemChecked(
        itemId: String,
        newChecked: Boolean
    ) {

        val lock =
            getLock(itemId)

        lock.withLock {

            val current =
                roomRepository.getItemById(itemId)
                    ?: return

            val now =
                System.currentTimeMillis()

            if (current.isChecked == newChecked) {
                return
            }

            val updated =
                current.copy(
                    isChecked = newChecked,
                    updatedAt = now
                )

            val entity =
                updated.toEntity()

            roomRepository.updateItem(entity)
        }
    }

    // ============================================================
    // UPDATE ITEM (NAME)
    // ============================================================

    suspend fun updateItem(
        item: ShoppingItem,
        newName: String
    ) {

        val lock =
            getLock(item.id)

        lock.withLock {

            val now =
                System.currentTimeMillis()

            val current =
                roomRepository.getItemById(item.id)
                    ?: return

            // ====================================================
            // HARD IDEMPOTENCY GUARD
            // ====================================================

            if (
                current.name == newName &&
                current.isChecked &&
                current.deletedAt == null
            ) {
                return
            }

            val updated =
                current.copy(
                    name = newName,
                    isChecked = true,
                    updatedAt = now
                )

            val entity =
                updated.toEntity()

            roomRepository.updateItem(entity)
        }
    }

    // ============================================================
    // DELETE ITEM
    // ============================================================

    suspend fun deleteItem(
        item: ShoppingItem
    ) {

        val lock =
            getLock(item.id)

        lock.withLock {

            val now =
                System.currentTimeMillis()

            val updated =
                item.copy(
                    deletedAt = now,
                    updatedAt = now
                )

            val entity =
                updated.toEntity()

            roomRepository.updateItem(entity)
        }
    }
}