package de.shopme.tools.knowledge.seasonality

class DefaultSeasonalityResolver(

    private val knowledge: SeasonalityKnowledge

) : SeasonalityResolver {

    override fun resolve(
        foodReference: String?
    ): List<Int> {

        val reference =
            foodReference ?: return emptyList()

        return knowledge.entries[
            reference.lowercase()
        ] ?: emptyList()

    }

}