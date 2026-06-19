package de.shopme.tools.knowledge.nutrition

interface NutritionAliasResolver {

    fun resolve(
        foodReference: String?
    ): String?

}