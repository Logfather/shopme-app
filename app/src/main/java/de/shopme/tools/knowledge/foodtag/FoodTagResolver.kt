package de.shopme.tools.knowledge.foodtag

import de.shopme.domain.food.FoodCategory
import de.shopme.domain.food.FoodTag

interface FoodTagResolver {

    fun resolve(
        category: FoodCategory
    ): Set<FoodTag>

}