package de.shopme.domain.recommendation.score

import de.shopme.domain.recommendation.RecommendationResult

class SimpleScoreCalculator : ScoreCalculator {

    override fun calculate(
        results: List<RecommendationResult>
    ): Int {

        return results.minOf {
            it.score
        }

    }
}