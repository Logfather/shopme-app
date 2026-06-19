package de.shopme.tools.knowledge.foodmiles

class DefaultFoodMilesResolver(

    private val knowledge:

    FoodMilesKnowledge

) : FoodMilesResolver {

    override fun resolve(

        foodReference: String?

    ): FoodMiles? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}