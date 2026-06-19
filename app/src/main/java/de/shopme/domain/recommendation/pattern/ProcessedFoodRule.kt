package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.shopping.ShoppingHistory

class ProcessedFoodRule {

    private val processedKeywords = setOf(

        "nutella",
        "cola",
        "chips",
        "schokolade",
        "kekse",
        "pizza",
        "fertiggericht",
        "energy",
        "limonade"

    )

    fun evaluate(
        history: ShoppingHistory
    ): RecommendationResult {

        val processedCount =
            history.previousPurchases.count { item ->

                processedKeywords.any {

                    item.lowercase().contains(it)

                }

            }

        return if (processedCount >= 3) {

            RecommendationResult(

                score = 80,

                reasons = listOf(
                    RecommendationReason.HIGH_PROCESSED_FOOD
                ),

                suggestions = listOf(
                    RecommendationSuggestion.ADD_FRUIT,
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