package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.shopping.ShoppingHistory

class VegetablePatternRule {

    private val vegetableKeywords = setOf(

        "tomate",
        "gurke",
        "paprika",
        "salat",
        "karotte",
        "möhre",
        "zwiebel",
        "brokkoli",
        "spinat",
        "zucchini"

    )

    fun evaluate(
        history: ShoppingHistory
    ): RecommendationResult {

        val vegetablePurchases =
            history.previousPurchases.count { item ->

                vegetableKeywords.any {

                    item.lowercase().contains(it)

                }

            }

        return if (vegetablePurchases == 0) {

            RecommendationResult(

                score = 85,

                reasons = listOf(
                    RecommendationReason.LOW_VEGETABLES
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