package de.shopme.domain.nutrition.model

data class NutritionProduct(
    val barcode: String,
    val name: String,
    val brand: String?,
    val category: String?,
    val imageUrl: String?,
    val nutrition: NutritionInfo?
)