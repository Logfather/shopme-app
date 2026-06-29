package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.nutrition.NutritionFacts

data class OFFKnowledgeCandidate(

    val catalogNormalizedName: String,

    val offCode: String?,

    val offProductName: String,

    val source: String,

    val dimensions: Set<KnowledgeDimensionId>,

    val nutritionFacts: NutritionFacts? = null
)