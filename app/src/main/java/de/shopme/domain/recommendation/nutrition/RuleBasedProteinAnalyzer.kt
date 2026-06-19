package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.shopbuddy.ShopBuddyRequest

class RuleBasedProteinAnalyzer : ProteinAnalyzer {

    override fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult {

        val protein =
            request.nutritionDetail.values.protein

        return if (protein >= 10.0) {

            RecommendationResult(

                score = 100,

                reasons = listOf(
                    RecommendationReason.GOOD_BALANCE
                ),

                suggestions = emptyList()

            )

        } else {

            RecommendationResult(

                score = 80,

                reasons = listOf(
                    RecommendationReason.LOW_PROTEIN
                ),

                suggestions = listOf(
                    RecommendationSuggestion.ADD_PROTEIN
                )

            )

        }
    }
}