package de.shopme.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.shopme.data.datasource.room.StoreTypeConverter

@Entity(tableName = "lists")
data class ShoppingListEntity(

    @PrimaryKey
    val id: String,

    val name: String,
    val ownerId: String,
    val storeTypes: List<StoreType>,

    val sharedWith: List<String> = emptyList(),

    val itemCount: Int = 0,

    val createdAt: Long,
    val updatedAt: Long
)