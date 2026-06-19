package de.shopme.tools.knowledge.ingredients

interface IngredientsResolver {

    fun resolve(
        foodReference: String?
    ): Set<String>

}