package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.shopping.ShoppingHistory

class ProteinPatternRule {

    private val proteinKeywords = setOf(

        "hähnchen",
        "huhn",
        "pute",
        "rind",
        "schwein",
        "lachs",
        "thunfisch",
        "ei",
        "eier",
        "quark",
        "skyr",
        "joghurt",
        "tofu",
        "linsen",
        "bohnen"

    )

    fun evaluate(
        history: ShoppingHistory
    ): RecommendationResult {

        val proteinPurchases =
            history.previousPurchases.count { item ->

                proteinKeywords.any {

                    item.lowercase().contains(it)

                }

            }

        return if (proteinPurchases == 0) {

            RecommendationResult(

                score = 85,

                reasons = listOf(
                    RecommendationReason.LOW_PROTEIN
                ),

                suggestions = listOf(
                    RecommendationSuggestion.ADD_PROTEIN
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