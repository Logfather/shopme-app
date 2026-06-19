package de.shopme.tools.knowledge.glycemic

import de.shopme.domain.food.GlycemicIndexLevel

class DefaultGlycemicIndexResolver(

    private val knowledge: GlycemicIndexKnowledge

) : GlycemicIndexResolver {

    override fun resolve(
        foodReference: String?
    ): GlycemicIndexLevel {

        val reference =
            foodReference ?: return GlycemicIndexLevel.UNKNOWN

        return knowledge.entries[
            reference.lowercase()
        ] ?: GlycemicIndexLevel.UNKNOWN

    }

}