package de.shopme.data

import com.google.firebase.Timestamp

data class ShoppingItemEntity(
    val id: String = "",
    val name: String = "",
    val quantity: Int = 1,
    val category: String = "Sonstiges",
    val isChecked: Boolean = false,
    val deletedAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val version: Int = 0
)