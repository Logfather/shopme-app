package de.shopme.domain.model

data class ShoppingList(

    val id: String,

    val name: String,

    val ownerId: String,

    val sharedWith: List<String>,

    val storeTypes: List<StoreType>,

    val itemCount: Int = 0,

    val createdAt: Long,

    val updatedAt: Long,

    val deletedAt: Long?
)