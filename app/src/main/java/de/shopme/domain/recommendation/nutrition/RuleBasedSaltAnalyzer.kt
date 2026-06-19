package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.shopbuddy.ShopBuddyRequest

class RuleBasedSaltAnalyzer : SaltAnalyzer {

    override fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult {

        val salt =
            request.nutritionDetail.values.salt

        return if (salt >= 1.5) {

            RecommendationResult(

                score = 40,

                reasons = listOf(
                    RecommendationReason.HIGH_SALT
                ),

                suggestions = emptyList()

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