package de.shopme.tools.knowledge.off

data class OFFProductCandidate(

    val id: String?,

    val productName: String,

    val normalizedName: String,

    val hasNutritionFacts: Boolean,

    val hasAllergens: Boolean
)