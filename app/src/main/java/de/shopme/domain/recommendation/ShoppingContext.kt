package de.shopme.domain.recommendation

import de.shopme.presentation.nutrition.model.NutritionDetail

data class ShoppingContext(

    val itemCount: Int,

    val categories: List<String>,

    val nutritionDetails: List<NutritionDetail>

    // später:
    // val dayOfWeek: DayOfWeek?
    // val season: Season?
    // val previousPurchases: List<...>
)