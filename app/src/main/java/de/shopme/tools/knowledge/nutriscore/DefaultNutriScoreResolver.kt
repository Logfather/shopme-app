package de.shopme.tools.knowledge.nutriscore

class DefaultNutriScoreResolver(

    private val knowledge: NutriScoreFactsKnowledge

) : NutriScoreResolver {

    override fun resolve(
        foodReference: String?
    ): NutriScore? {

        val reference = foodReference ?: return null

        return knowledge.entries[
            reference
        ]
    }

}