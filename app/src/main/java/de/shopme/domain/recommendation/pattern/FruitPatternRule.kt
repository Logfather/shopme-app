package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.statistics.WeeklyStatistics

class FruitPatternRule {

    private val fruitKeywords = setOf(

        "apfel",
        "banane",
        "birne",
        "orange",
        "mandarine",
        "erdbeere",
        "kiwi"

    )

    fun evaluate(
        statistics: WeeklyStatistics
    ): RecommendationResult {

        return if (statistics.fruitCount == 0) {

            RecommendationResult(

                score = 85,

                reasons = listOf(
                    RecommendationReason.LOW_FRUIT
                ),

                suggestions = listOf(
                    RecommendationSuggestion.ADD_FRUIT
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