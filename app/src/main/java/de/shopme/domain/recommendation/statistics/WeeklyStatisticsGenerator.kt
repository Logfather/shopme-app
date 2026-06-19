package de.shopme.domain.recommendation.statistics

import de.shopme.domain.recommendation.shopping.ShoppingHistory
import de.shopme.domain.recommendation.statistics.calculator.DiversityCalculator
import de.shopme.domain.recommendation.statistics.calculator.ShoppingVarietyCalculator
import de.shopme.domain.recommendation.statistics.counter.FruitStatisticsCounter
import de.shopme.domain.recommendation.statistics.counter.ProcessedFoodStatisticsCounter
import de.shopme.domain.recommendation.statistics.counter.ProteinStatisticsCounter
import de.shopme.domain.recommendation.statistics.counter.VegetableStatisticsCounter

class WeeklyStatisticsGenerator(

    private val fruitCounter: FruitStatisticsCounter,

    private val vegetableCounter: VegetableStatisticsCounter,

    private val proteinCounter: ProteinStatisticsCounter,

    private val processedFoodCounter: ProcessedFoodStatisticsCounter,

    private val diversityCalculator: DiversityCalculator,

    private val shoppingVarietyCalculator:
    ShoppingVarietyCalculator,

    private val builder: WeeklyStatisticsBuilder

){

    fun generate(
        history: ShoppingHistory
    ): WeeklyStatistics {

        val purchases =
            history.previousPurchases

        val fruitCount =
            fruitCounter.count(purchases)

        val vegetableCount =
            vegetableCounter.count(purchases)

        val proteinCount =
            proteinCounter.count(purchases)

        val processedFoodCount =
            processedFoodCounter.count(purchases)

        val diversityScore =
            diversityCalculator.calculate(purchases)

        val shoppingVarietyScore =
            shoppingVarietyCalculator.calculate(
                purchases
            )

        return builder.build(

            fruitCount = fruitCount,

            vegetableCount = vegetableCount,

            proteinCount = proteinCount,

            processedFoodCount = processedFoodCount,

            diversityScore = diversityScore,

            shoppingVarietyScore = shoppingVarietyScore

        )

    }

    companion object {

        private val fruitKeywords = setOf(
            "apfel",
            "banane",
            "birne",
            "orange",
            "kiwi",
            "erdbeere"
        )

        private val vegetableKeywords = setOf(
            "tomate",
            "gurke",
            "paprika",
            "salat",
            "brokkoli",
            "spinat"
        )

        private val proteinKeywords = setOf(
            "ei",
            "eier",
            "quark",
            "skyr",
            "tofu",
            "lachs",
            "hähnchen",
            "bohnen"
        )

        private val processedKeywords = setOf(
            "cola",
            "chips",
            "pizza",
            "nutella",
            "kekse",
            "schokolade"
        )

    }

}