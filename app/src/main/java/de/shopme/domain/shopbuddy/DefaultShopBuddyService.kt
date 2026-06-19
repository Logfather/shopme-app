package de.shopme.domain.shopbuddy

import de.shopme.domain.recommendation.RecommendationGenerator
import de.shopme.domain.recommendation.RuleFormatter

class DefaultShopBuddyService(

    private val recommendationGenerator: RecommendationGenerator,

    private val ruleFormatter: RuleFormatter

) : ShopBuddyService {

    override suspend fun getAdvice(
        request: ShopBuddyRequest
    ): ShopBuddyAdvice {

        val recommendation =
            recommendationGenerator.generate(request)

        return ruleFormatter.format(recommendation)
    }
}