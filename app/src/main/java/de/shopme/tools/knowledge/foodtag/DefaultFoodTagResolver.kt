package de.shopme.tools.knowledge.foodtag

import de.shopme.domain.food.FoodCategory
import de.shopme.domain.food.FoodTag

class DefaultFoodTagResolver : FoodTagResolver {

    override fun resolve(
        category: FoodCategory
    ): Set<FoodTag> {

        return when (category) {

            FoodCategory.FRUIT -> setOf(
                FoodTag.FRUIT,
                FoodTag.PLANT_BASED,
                FoodTag.FRESH,
                FoodTag.HEALTHY
            )

            FoodCategory.VEGETABLE -> setOf(
                FoodTag.VEGETABLE,
                FoodTag.PLANT_BASED,
                FoodTag.FRESH,
                FoodTag.HEALTHY
            )

            FoodCategory.PROTEIN -> setOf(
                FoodTag.PROTEIN,
                FoodTag.ANIMAL
            )

            FoodCategory.DAIRY -> setOf(
                FoodTag.DAIRY,
                FoodTag.ANIMAL
            )

            FoodCategory.GRAIN -> setOf(
                FoodTag.GRAIN,
                FoodTag.PLANT_BASED
            )

            FoodCategory.BEVERAGE -> setOf(
                FoodTag.BEVERAGE
            )

            FoodCategory.SWEETS -> setOf(
                FoodTag.SWEET,
                FoodTag.PROCESSED
            )

            FoodCategory.NUTS -> setOf(
                FoodTag.NUTS,
                FoodTag.PLANT_BASED,
                FoodTag.HEALTHY
            )

            FoodCategory.LEGUMES -> setOf(
                FoodTag.LEGUMES,
                FoodTag.PLANT_BASED,
                FoodTag.PROTEIN,
                FoodTag.HEALTHY
            )

            FoodCategory.PROCESSED -> setOf(
                FoodTag.PROCESSED
            )

            FoodCategory.UNKNOWN ->
                emptySet()

        }

    }

}