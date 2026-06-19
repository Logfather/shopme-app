package de.shopme.tools.knowledge.seasonality

interface SeasonalityResolver {

    fun resolve(
        foodReference: String?
    ): List<Int>

}