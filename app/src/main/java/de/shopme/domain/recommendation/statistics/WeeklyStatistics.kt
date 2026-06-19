package de.shopme.domain.recommendation.statistics

data class WeeklyStatistics(

    val fruitCount: Int,

    val vegetableCount: Int,

    val proteinCount: Int,

    val processedFoodCount: Int,

    val diversityScore: Int,

    val shoppingVarietyScore: Int

)