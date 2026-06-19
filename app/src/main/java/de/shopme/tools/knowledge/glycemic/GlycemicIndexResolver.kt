package de.shopme.tools.knowledge.glycemic

import de.shopme.domain.food.GlycemicIndexLevel

interface GlycemicIndexResolver {

    fun resolve(
        foodReference: String?
    ): GlycemicIndexLevel

}