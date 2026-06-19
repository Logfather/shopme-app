package de.shopme.data.nutrition.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "nutrition_reference_mapping"
)
data class NutritionReferenceMappingEntity(

    @PrimaryKey
    val reference: String,

    val barcode: String,

    val updatedAt: Long
)