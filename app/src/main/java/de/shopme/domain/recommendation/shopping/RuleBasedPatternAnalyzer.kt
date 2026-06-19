package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.shopbuddy.ShopBuddyRequest

class RuleBasedPatternAnalyzer : PatternAnalyzer {

    override fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult {

        return RecommendationResult(

            score = 100,

            reasons = emptyList(),

            suggestions = emptyList()

        )

    }

}