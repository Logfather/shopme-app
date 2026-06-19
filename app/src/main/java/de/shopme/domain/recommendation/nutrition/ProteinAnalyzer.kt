package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.shopbuddy.ShopBuddyRequest

interface ProteinAnalyzer {

    fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult

}