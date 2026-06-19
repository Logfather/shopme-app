package de.shopme.tools.knowledge.waterfootprint

interface WaterResolver {

    fun resolve(
        foodReference: String?
    ): WaterFootprint?

}