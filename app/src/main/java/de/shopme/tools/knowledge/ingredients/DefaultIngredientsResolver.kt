package de.shopme.tools.knowledge.ingredients

class DefaultIngredientsResolver(

    private val knowledge: IngredientsKnowledge

) : IngredientsResolver {

    override fun resolve(
        foodReference: String?
    ): Set<String> {

        val reference =
            foodReference ?: return emptySet()

        return knowledge.entries[
            reference.lowercase()
        ] ?: emptySet()

    }

}