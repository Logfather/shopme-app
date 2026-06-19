package de.shopme.domain.nutrition.model

import de.shopme.tools.knowledge.nutriscore.NutriScore
data class NutritionSearchResult(
    val barcode: String,
    val name: String,
    val brand: String?,
    val nutriScore: NutriScore?,
    val confidence: Float
)