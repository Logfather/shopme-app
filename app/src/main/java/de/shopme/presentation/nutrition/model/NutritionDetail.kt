package de.shopme.presentation.nutrition.model

import de.shopme.presentation.components.ShopBuddyState
import de.shopme.tools.knowledge.nutrition.NutritionFacts

data class NutritionDetail(

    val productName: String,

    val nutriScore: String,

    val values: NutritionFacts,

    val infoTitle: String,

    val infoText: String,

    val buddyState: ShopBuddyState

)