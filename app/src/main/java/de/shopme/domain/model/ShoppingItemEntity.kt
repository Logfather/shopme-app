package de.shopme.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class ShoppingItemEntity(

    @PrimaryKey
    val id: String,

    val listId: String,

    val name: String,

    val quantity: Int,

    val category: String,

    val isChecked: Boolean,

    val deletedAt: Long?,

    val createdAt: Long,

    val updatedAt: Long

)