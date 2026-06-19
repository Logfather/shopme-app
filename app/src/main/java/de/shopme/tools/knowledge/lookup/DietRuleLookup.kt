package de.shopme.tools.knowledge.lookup

import de.shopme.domain.food.FoodTag
import de.shopme.tools.knowledge.diet.DietClassification

interface DietRuleLookup {

    fun forbiddenTags(

        classification: DietClassification

    ): Set<FoodTag>

}