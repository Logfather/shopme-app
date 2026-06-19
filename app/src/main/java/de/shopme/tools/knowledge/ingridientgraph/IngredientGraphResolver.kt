package de.shopme.tools.knowledge.ingredientgraph

interface IngredientGraphResolver {

    fun resolve(

        foodReference: String?

    ): IngredientGraphEntry?

}