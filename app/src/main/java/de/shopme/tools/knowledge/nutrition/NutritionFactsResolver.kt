package de.shopme.tools.knowledge.nutrition

interface NutritionFactsResolver {

    fun resolve(
        foodReference: String?
    ): NutritionFacts?

}