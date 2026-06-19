package de.shopme.tools.knowledge.foodcategory

import de.shopme.domain.food.FoodCategory

interface FoodCategoryResolver {

    fun resolve(
        supermarketCategory: String?
    ): FoodCategory

}