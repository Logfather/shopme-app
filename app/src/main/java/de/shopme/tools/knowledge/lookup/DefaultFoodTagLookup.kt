package de.shopme.tools.knowledge.lookup

import de.shopme.domain.food.FoodTag

class DefaultFoodTagLookup(

    private val tags: Map<String, Set<FoodTag>>

) : FoodTagLookup {

    override fun lookup(

        food: String

    ): Set<FoodTag> {

        return tags[food]

            ?: emptySet()

    }

}