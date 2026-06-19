package de.shopme.tools.knowledge.recipegraph

interface RecipeGraphResolver {

    fun resolve(

        recipeReference: String?

    ): RecipeGraphEntry?

}