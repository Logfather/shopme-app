package de.shopme.tools.knowledge.waterfootprint

class DefaultWaterResolver(

    private val knowledge: WaterKnowledge

) : WaterResolver {

    override fun resolve(
        foodReference: String?
    ): WaterFootprint? {

        val reference =
            foodReference ?: return null

        return knowledge.entries[
            reference.lowercase()
        ]

    }

}