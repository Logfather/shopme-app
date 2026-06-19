package de.shopme.tools.knowledge.recipegraph

class DefaultRecipeGraphResolver(

    private val knowledge:

    RecipeGraphKnowledge

) : RecipeGraphResolver {

    override fun resolve(

        recipeReference: String?

    ): RecipeGraphEntry? {

        val reference =

            recipeReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}