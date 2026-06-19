package de.shopme.domain.nutrition.model

import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutrition.NutritionFacts

data class NutritionInfo(

    val barcode: String,

    val nutriScore: NutriScore?,

    val facts: NutritionFacts,

    val lastUpdated: Long

)