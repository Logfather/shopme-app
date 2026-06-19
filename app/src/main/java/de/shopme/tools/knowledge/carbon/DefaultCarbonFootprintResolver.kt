package de.shopme.tools.knowledge.carbon

class DefaultCarbonFootprintResolver(

    private val knowledge: CarbonKnowledge

) : CarbonFootprintResolver {

    override fun resolve(
        foodReference: String?
    ): CarbonFootprint? {

        val reference =
            foodReference ?: return null

        return knowledge.entries[
            reference.lowercase()
        ]

    }

}