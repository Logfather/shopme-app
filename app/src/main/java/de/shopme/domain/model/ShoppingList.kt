package de.shopme.domain.model

data class ShoppingList(
    val id: String,
    val name: String,
    val ownerId: String,
    val storeTypes: List<StoreType>,
    val createdAt: Long,
    val updatedAt: Long
)