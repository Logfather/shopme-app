package de.shopme.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.shopme.data.local.StoreTypeConverter

@Entity(tableName = "lists")
@TypeConverters(StoreTypeConverter::class)
data class ShoppingListEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val ownerId: String,

    val storeTypes: List<StoreType>,

    val createdAt: Long,

    val updatedAt: Long

)