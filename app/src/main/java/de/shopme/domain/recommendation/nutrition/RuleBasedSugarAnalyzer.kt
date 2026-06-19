package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.shopbuddy.ShopBuddyRequest

class RuleBasedSugarAnalyzer : SugarAnalyzer {

    override fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult {

        val sugar =
            request.nutritionDetail.values.sugar

        return if (sugar >= 20.0) {

            RecommendationResult(

                score = 20,

                reasons = listOf(
                    RecommendationReason.HIGH_SUGAR
                ),

                suggestions = listOf(
                    RecommendationSuggestion.REDUCE_SUGAR
                )

            )

        } else {

            RecommendationResult(

                score = 100,

                reasons = emptyList(),

                suggestions = emptyList()

            )

        }
    }
}