package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.shopping.ShoppingHistory

class SugarPatternRule {

    fun evaluate(
        history: ShoppingHistory
    ): RecommendationResult {

        val sugarKeywords = setOf(

            "cola",
            "nutella",
            "schokolade",
            "kekse",
            "gummibärchen",
            "chips"

        )

        val count = history.previousPurchases.count { item ->

            sugarKeywords.any {

                item.lowercase().contains(it)

            }

        }

        return if (count >= 3) {

            RecommendationResult(

                score = 80,

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