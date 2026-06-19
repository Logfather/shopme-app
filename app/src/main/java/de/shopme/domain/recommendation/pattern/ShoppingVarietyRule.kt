package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.shopping.ShoppingHistory

class ShoppingVarietyRule {

    fun evaluate(
        history: ShoppingHistory
    ): RecommendationResult {

        val totalPurchases =
            history.previousPurchases.size

        val uniquePurchases =
            history.previousPurchases
                .map { it.lowercase().trim() }
                .distinct()
                .size

        return if (

            totalPurchases > 0 &&
            uniquePurchases * 2 < totalPurchases

        ) {

            RecommendationResult(

                score = 85,

                reasons = listOf(
                    RecommendationReason.GOOD_BALANCE
                ),

                suggestions = listOf(
                    RecommendationSuggestion.ADD_VEGETABLES
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