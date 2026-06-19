package de.shopme.tools.knowledge.recipe

interface RecipeResolver {

    fun resolve(
        foodReference: String?
    ): List<String>

}