package de.shopme.tools.knowledge.carbon

class DefaultCarbonImpactResolver(

    private val knowledge:

    CarbonImpactKnowledge

) {

    fun resolve(

        foodReference: String?

    ): CarbonImpactLevel? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}