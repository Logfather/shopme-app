package de.shopme.domain.recommendation.shopping

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion

class ShoppingBalanceRule {

    fun evaluate(

        categoryCount: Int,

        fruitCount: Int,

        vegetableCount: Int

    ): RecommendationResult {

        return if (

            categoryCount >= 4 &&
            fruitCount >= 1 &&
            vegetableCount >= 1

        ) {

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

                suggestions = buildList {

                    if (fruitCount == 0) {
                        add(
                            RecommendationSuggestion.ADD_FRUIT
                        )
                    }

                    if (vegetableCount == 0) {
                        add(
                            RecommendationSuggestion.ADD_VEGETABLES
                        )
                    }

                }

            )

        }

    }

}