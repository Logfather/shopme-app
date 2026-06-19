package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.shopbuddy.ShopBuddyRequest

interface SaltAnalyzer {

    fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult

}