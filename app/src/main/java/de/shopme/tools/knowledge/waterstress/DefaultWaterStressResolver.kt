package de.shopme.tools.knowledge.waterstress

class DefaultWaterStressResolver(

    private val knowledge:

    WaterStressKnowledge

) : WaterStressResolver {

    override fun resolve(

        foodReference: String?

    ): WaterStress? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}