package de.shopme.tools.knowledge.semantics

class DefaultFoodSemanticsResolver(

    private val knowledge: FoodSemanticsKnowledge

) : FoodSemanticsResolver {

    override fun resolve(
        taxonomyNode: String?
    ): FoodSemanticsEntry? {

        val node =
            taxonomyNode ?: return null

        return knowledge.entries[
            node.lowercase()
        ]

    }

}