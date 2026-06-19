package de.shopme.domain.recommendation

import de.shopme.domain.shopbuddy.ShopBuddyAdvice

interface RuleFormatter {

    fun format(
        result: RecommendationResult
    ): ShopBuddyAdvice

}