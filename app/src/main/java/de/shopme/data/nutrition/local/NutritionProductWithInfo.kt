package de.shopme.data.nutrition.local

import androidx.room.Embedded
import androidx.room.Relation

data class NutritionProductWithInfo(

    @Embedded
    val product: NutritionProductEntity,

    @Relation(
        parentColumn = "barcode",
        entityColumn = "barcode"
    )
    val nutrition: NutritionInfoEntity?
)