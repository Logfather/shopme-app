package de.shopme.tools.knowledge.lookup

interface IngredientLookup {

    fun lookup(

        food: String

    ): Set<String>

}