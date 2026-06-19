package de.shopme.tools.knowledge.semantics

import de.shopme.domain.food.FoodCategory
import de.shopme.domain.food.FoodTag

data class FoodSemanticsEntry(

    val category: FoodCategory?,

    val tags: Set<FoodTag>

)