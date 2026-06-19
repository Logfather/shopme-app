package de.shopme.domain.recommendation.nutrition

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.shopbuddy.ShopBuddyRequest

interface NutritionAnalyzer {

    fun analyze(
        request: ShopBuddyRequest
    ): RecommendationResult

}