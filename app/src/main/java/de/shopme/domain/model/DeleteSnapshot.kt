package de.shopme.domain.model

data class ListDeleteSnapshot(
    val list: ShoppingListEntity,
    val items: List<ShoppingItemEntity>,
    val timestamp: Long
)