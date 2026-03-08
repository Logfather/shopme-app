package de.shopme.domain.model

data class ShoppingItem(
    val id: String,
    val name: String,
    val quantity: Int,
    val category: String,
    val isChecked: Boolean,
    val version: Int,
    val deletedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)