package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.shopping.ShoppingHistory

class DiversityRule {

    fun evaluate(
        history: ShoppingHistory
    ): RecommendationResult {

        val uniqueProducts =
            history.previousPurchases
                .map { it.lowercase().trim() }
                .distinct()
                .size

        return if (uniqueProducts >= 10) {

            RecommendationResult(

                score = 100,

                reasons = listOf(
                    RecommendationReason.GOOD_BALANCE
                ),

                suggestions = emptyList()

            )

        } else {

            RecommendationResult(

                score = 90,

                reasons = emptyList(),

                suggestions = listOf(
                    RecommendationSuggestion.ADD_VEGETABLES
                )

            )

        }

    }

}