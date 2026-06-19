package de.shopme.data.nutrition.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutrition_info",
    foreignKeys = [
        ForeignKey(
            entity = NutritionProductEntity::class,
            parentColumns = ["barcode"],
            childColumns = ["barcode"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NutritionInfoEntity(

    @PrimaryKey
    val barcode: String,

    val nutriScore: String,

    val caloriesPer100g: Double?,

    val fatPer100g: Double?,

    val saturatedFatPer100g: Double?,

    val carbohydratesPer100g: Double?,

    val sugarPer100g: Double?,

    val fiberPer100g: Double?,

    val proteinPer100g: Double?,

    val saltPer100g: Double?,

    val updatedAt: Long
)