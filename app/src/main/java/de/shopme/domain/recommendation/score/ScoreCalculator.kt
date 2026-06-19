package de.shopme.domain.recommendation.score

import de.shopme.domain.recommendation.RecommendationResult

interface ScoreCalculator {

    fun calculate(
        results: List<RecommendationResult>
    ): Int

}