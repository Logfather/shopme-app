package de.shopme.tools.knowledge.carbon

interface CarbonFootprintResolver {

    fun resolve(
        foodReference: String?
    ): CarbonFootprint?

}