package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationReason
import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.RecommendationSuggestion
import de.shopme.domain.recommendation.score.ScoreCalculator
import de.shopme.domain.shopbuddy.ShopBuddyRequest

class RuleBasedNutritionAnalyzer(

    private val sugarAnalyzer: SugarAnalyzer,

    private val fatAnalyzer: FatAnalyzer,

    private val saltAnalyzer: SaltAnalyzer,

    private val proteinAnalyzer: ProteinAnalyzer,

    private val scoreCalculator: ScoreCalculator

) : NutritionAnalyzer {

    override fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult {

        val nutriScoreResult =
            evaluateNutriScore(request)

        val sugarResult =
            sugarAnalyzer.analyze(request)

        val fatResult =
            fatAnalyzer.analyze(request)

        val saltResult =
            saltAnalyzer.analyze(request)

        val proteinResult =
            proteinAnalyzer.analyze(request)

        val score = scoreCalculator.calculate(

            listOf(

                nutriScoreResult,

                sugarResult,

                fatResult,

                saltResult,

                proteinResult

            )

        )

        return RecommendationResult(

            score = score,

            reasons = (
                    nutriScoreResult.reasons +
                            sugarResult.reasons +
                            fatResult.reasons +
                            saltResult.reasons +
                            proteinResult.reasons
                    ).distinct(),

            suggestions = (
                    nutriScoreResult.suggestions +
                            sugarResult.suggestions +
                            fatResult.suggestions +
                            saltResult.suggestions +
                            proteinResult.suggestions
                    ).distinct()

        )
    }

    private fun evaluateNutriScore(
        request: ShopBuddyRequest
    ): RecommendationResult {

        return when (request.nutritionDetail.nutriScore) {

            "A" -> RecommendationResult(
                score = 90,
                reasons = listOf(
                    RecommendationReason.HEALTHY_PRODUCT,
                    RecommendationReason.GOOD_BALANCE
                ),
                suggestions = listOf(
                    RecommendationSuggestion.KEEP_CURRENT_SELECTION
                )
            )

            "B" -> RecommendationResult(
                score = 75,
                reasons = listOf(
                    RecommendationReason.GOOD_BALANCE
                ),
                suggestions = listOf(
                    RecommendationSuggestion.KEEP_CURRENT_SELECTION
                )
            )

            "C" -> RecommendationResult(
                score = 55,
                reasons = listOf(
                    RecommendationReason.GOOD_BALANCE
                ),
                suggestions = listOf(
                    RecommendationSuggestion.ADD_FRUIT
                )
            )

            "D" -> RecommendationResult(
                score = 35,
                reasons = listOf(
                    RecommendationReason.HIGH_FAT
                ),
                suggestions = listOf(
                    RecommendationSuggestion.REDUCE_FAT
                )
            )

            "E" -> RecommendationResult(
                score = 15,
                reasons = listOf(
                    RecommendationReason.HIGH_SUGAR,
                    RecommendationReason.HIGH_PROCESSED_FOOD
                ),
                suggestions = listOf(
                    RecommendationSuggestion.REDUCE_SUGAR,
                    RecommendationSuggestion.ADD_FRUIT
                )
            )

            else -> RecommendationResult(
                score = 50,
                reasons = emptyList(),
                suggestions = emptyList()
            )
        }
    }
}