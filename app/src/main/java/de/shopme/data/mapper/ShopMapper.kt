package de.shopme.data.mapper

import de.shopme.domain.model.*

object ShopMapper {

    // ---------------------------
    // LIST
    // ---------------------------

    fun fromEntity(entity: ShoppingListEntity): ShoppingList =
        ShoppingList(
            id = entity.id,
            name = entity.name,
            ownerId = entity.ownerId,
            storeTypes = entity.storeTypes,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

    fun toEntity(domain: ShoppingList): ShoppingListEntity =
        ShoppingListEntity(
            id = domain.id,
            name = domain.name,
            ownerId = domain.ownerId,
            storeTypes = domain.storeTypes,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )

    // ---------------------------
    // ITEM
    // ---------------------------

    fun fromEntity(entity: ShoppingItemEntity): ShoppingItem =
        ShoppingItem(
            id = entity.id,
            name = entity.name,
            quantity = entity.quantity,
            category = entity.category,
            isChecked = entity.isChecked,
            version = entity.version,
            deletedAt = entity.deletedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )

    fun toEntity(domain: ShoppingItem): ShoppingItemEntity =
        ShoppingItemEntity(
            id = domain.id,
            name = domain.name,
            quantity = domain.quantity,
            category = domain.category,
            isChecked = domain.isChecked,
            version = domain.version,
            deletedAt = domain.deletedAt,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt)
}