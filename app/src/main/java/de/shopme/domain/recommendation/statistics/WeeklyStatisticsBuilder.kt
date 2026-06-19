package de.shopme.domain.recommendation.statistics

class WeeklyStatisticsBuilder {

    fun build(

        fruitCount: Int,

        vegetableCount: Int,

        proteinCount: Int,

        processedFoodCount: Int,

        diversityScore: Int,

        shoppingVarietyScore: Int

    ): WeeklyStatistics {

        return WeeklyStatistics(

            fruitCount = fruitCount,

            vegetableCount = vegetableCount,

            proteinCount = proteinCount,

            processedFoodCount = processedFoodCount,

            diversityScore = diversityScore,

            shoppingVarietyScore = shoppingVarietyScore

        )

    }

}