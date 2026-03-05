package de.shopme.domain.model

data class ShoppingListEntity(
    val id: String,
    val name: String,
    val ownerId: String,
    val storeTypes: List<StoreType>,
    val isCustom: Boolean = false,
    val itemCount: Int = 0
)