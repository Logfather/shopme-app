package de.shopme.tools.knowledge.lookup

import de.shopme.domain.food.FoodTag
import de.shopme.tools.knowledge.diet.DietClassification

class DefaultDietRuleLookup : DietRuleLookup {

    private val rules = mapOf(

        DietClassification.VEGAN to setOf(

            FoodTag.ANIMAL

        ),

        DietClassification.VEGETARIAN to setOf(

            FoodTag.MEAT,
            FoodTag.FISH

        ),

        DietClassification.PESCETARIAN to setOf(

            FoodTag.MEAT

        )

    )

    override fun forbiddenTags(

        classification: DietClassification

    ): Set<FoodTag> =

        rules[classification]

            ?: emptySet()

}