package de.shopme.domain.recommendation

import de.shopme.domain.shopbuddy.ShopBuddyRequest

interface RecommendationGenerator {

    fun generate(
        request: ShopBuddyRequest
    ): RecommendationResult

}