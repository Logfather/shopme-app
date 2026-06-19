package de.shopme.domain.recommendation.statistics.calculator

class ShoppingVarietyCalculator {

    fun calculate(
        purchases: List<String>
    ): Int {

        if (purchases.isEmpty()) {
            return 0
        }

        val uniquePurchases =
            purchases
                .map {
                    it.lowercase().trim()
                }
                .distinct()
                .size

        return uniquePurchases * 100 / purchases.size

    }

}