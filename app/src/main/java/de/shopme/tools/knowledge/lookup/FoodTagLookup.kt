package de.shopme.tools.knowledge.lookup

import de.shopme.domain.food.FoodTag

interface FoodTagLookup {

    fun lookup(

        food: String

    ): Set<FoodTag>

}