package de.shopme.data.nutrition.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutrition_products"
)
data class NutritionProductEntity(

    @PrimaryKey
    val barcode: String,

    val name: String,

    val brand: String?,

    val category: String?,

    val imageUrl: String?,

    val updatedAt: Long
)