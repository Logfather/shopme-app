package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.shopbuddy.ShopBuddyRequest

interface SugarAnalyzer {

    fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult

}