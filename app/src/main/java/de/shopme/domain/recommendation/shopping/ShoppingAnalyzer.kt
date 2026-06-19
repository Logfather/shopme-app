package de.shopme.domain.recommendation.shopping

import de.shopme.domain.recommendation.RecommendationResult
import de.shopme.domain.recommendation.ShoppingContext

interface ShoppingAnalyzer {

    fun analyze(
        context: ShoppingContext
    ): RecommendationResult

}