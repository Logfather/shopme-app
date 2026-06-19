package de.shopme.tools.knowledge.foodmiles

interface FoodMilesResolver {

    fun resolve(

        foodReference: String?

    ): FoodMiles?

}