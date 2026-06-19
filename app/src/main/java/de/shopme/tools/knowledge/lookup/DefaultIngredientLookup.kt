package de.shopme.tools.knowledge.lookup

import de.shopme.tools.knowledge.ingredients.IngredientsKnowledge

class DefaultIngredientLookup(

    private val knowledge: IngredientsKnowledge

) : IngredientLookup {

    override fun lookup(

        food: String

    ): Set<String> {

        return knowledge.entries[food]

            ?.toSet()

            ?: emptySet()

    }

}