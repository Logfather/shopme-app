package de.shopme.presentation.components.foodintelligence

data class FoodDimension(

    val section: FoodIntelligenceSection,

    val displayOrder: Int,

    val category: FoodDimensionCategory,

    val title: String,

    val shortTitle: String,

    val description: String

)