package de.shopme.tools.knowledge.ingredientgraph

class DefaultIngredientGraphResolver(

    private val knowledge:

    IngredientGraphKnowledge

) : IngredientGraphResolver {

    override fun resolve(

        foodReference: String?

    ): IngredientGraphEntry? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}