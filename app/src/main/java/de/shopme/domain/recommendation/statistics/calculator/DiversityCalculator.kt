package de.shopme.domain.recommendation.statistics.calculator

class DiversityCalculator {

    fun calculate(
        purchases: List<String>
    ): Int {

        return purchases
            .map {
                it.lowercase().trim()
            }
            .distinct()
            .size

    }

}