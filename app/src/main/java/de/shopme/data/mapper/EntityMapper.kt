package de.shopme.data.mapper

import de.shopme.domain.model.*

object EntityMapper {

    // ---------------------------
    // LIST
    // ---------------------------

    fun ShoppingListEntity.toDomain(): ShoppingList =
        ShoppingList(
            id = id,
            name = name,
            ownerId = ownerId,
            storeTypes = storeTypes,
            itemCount = itemCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    fun ShoppingList.toEntity(): ShoppingListEntity =
        ShoppingListEntity(
            id = id,
            name = name,
            ownerId = ownerId,
            storeTypes = storeTypes,
            itemCount = itemCount,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    // ---------------------------
    // ITEM
    // ---------------------------

    fun ShoppingItemEntity.toDomain(): ShoppingItem {
        return ShoppingItem(
            id = id,
            listId = listId,   // ✅ FIX
            name = name,
            quantity = quantity,
            category = category,
            isChecked = isChecked,
            version = version,
            deletedAt = deletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    fun ShoppingItem.toEntity(): ShoppingItemEntity {
        return ShoppingItemEntity(
            id = id,
            listId = listId,   // ✅ FIX
            name = name,
            quantity = quantity,
            category = category,
            isChecked = isChecked,
            version = version,
            deletedAt = deletedAt,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}