package de.shopme.domain.recommendation

import de.shopme.domain.recommendation.nutrition.NutritionAnalyzer
import de.shopme.domain.recommendation.pattern.PatternAnalyzer
import de.shopme.domain.recommendation.shopping.ShoppingAnalyzer
import de.shopme.domain.shopbuddy.ShopBuddyRequest

class RuleBasedRecommendationGenerator(

    private val nutritionAnalyzer: NutritionAnalyzer,

    private val shoppingAnalyzer: ShoppingAnalyzer,

    private val patternAnalyzer: PatternAnalyzer,

    private val recommendationAggregator: RecommendationAggregator

) : RecommendationGenerator {

    override fun generate(
        request: ShopBuddyRequest
    ): RecommendationResult {

        val nutritionResult =
            nutritionAnalyzer.analyze(request)

        val shoppingResult =
            shoppingAnalyzer.analyze(

                ShoppingContext(

                    itemCount = 1,

                    categories = emptyList(),

                    nutritionDetails = listOf(
                        request.nutritionDetail
                    )

                )

            )

        val patternResult =
            patternAnalyzer.analyze(request)

        return recommendationAggregator.aggregate(

            listOf(

                nutritionResult,

                shoppingResult,

                patternResult

            )

        )

    }

}