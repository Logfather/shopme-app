package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.nutrition.NutritionFacts

data class OFFKnowledgeExtractionResult(

    val normalizedName: String,

    val sourceId: String?,

    val productName: String,

    val source: String = "open_food_facts",

    val availableDimensions: Set<KnowledgeDimensionId>,

    val nutritionFacts: NutritionFacts?,

    val allergens: Set<String>,

    val ingredients: List<String>,

    val packaging: Set<String>,

    val labels: Set<String>,

    val categories: Set<String>,

    val countries: Set<String>
)