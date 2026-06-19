package de.shopme.domain.recommendation.pattern

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.shopbuddy.ShopBuddyRequest

interface PatternAnalyzer {

    fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult

}