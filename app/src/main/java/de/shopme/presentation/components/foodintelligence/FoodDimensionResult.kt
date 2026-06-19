package de.shopme.presentation.components.foodintelligence

import de.shopme.ui.icons.TrafficLight

data class FoodDimensionResult(

    val dimension: FoodDimension,

    val trafficLight: TrafficLight,

    val summary: String,

    val recommendation: String

)