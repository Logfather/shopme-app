package de.shopme.domain.recommendation.shopping

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.ShoppingContext
import de.shopme.domain.recommendation.statistics.counter.FruitStatisticsCounter
import de.shopme.domain.recommendation.statistics.counter.VegetableStatisticsCounter

class RuleBasedShoppingAnalyzer(

    private val categoryCounter: ShoppingCategoryCounter,

    private val fruitCounter: FruitStatisticsCounter,

    private val vegetableCounter: VegetableStatisticsCounter,

    private val shoppingBalanceRule: ShoppingBalanceRule

) : ShoppingAnalyzer {

    override fun analyze(
        context: ShoppingContext
    ): RecommendationResult {

        val categoryCount =
            categoryCounter.count(context)

        val purchases =
            context.nutritionDetails.map { detail ->

                detail.productName

            }

        val fruitCount =
            fruitCounter.count(purchases)

        val vegetableCount =
            vegetableCounter.count(purchases)

        return shoppingBalanceRule.evaluate(

            categoryCount = categoryCount,

            fruitCount = fruitCount,

            vegetableCount = vegetableCount

        )

    }

}