package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.shopbuddy.ShopBuddyRequest

class RuleBasedFatAnalyzer : FatAnalyzer {

    override fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult {

        val fat =
            request.nutritionDetail.values.fat

        return if (fat >= 20.0) {

            RecommendationResult(

                score = 30,

                reasons = listOf(
                    RecommendationReason.HIGH_FAT
                ),

                suggestions = listOf(
                    RecommendationSuggestion.REDUCE_FAT
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