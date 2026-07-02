package de.shopme.tools.knowledge.ai.builder.off

data class OFFCandidateData(
    val sourceId: String,
    val name: String?,
    val nutrition: Any?,
    val ingredients: Any?,
    val taxonomy: Any?,
    val allergens: Any?,
    val packaging: Any?,
    val production: Any?,
    val locality: Any?,
    val glycemic: Any?,
    val water: Any?,
    val carbon: Any?,
    val waterStress: Any?,
    val biodiversity: Any?,
    val pollinator: Any?,
    val fairtrade: Any?,
    val animalWelfare: Any?,
    val seasonality: Any?,
    val foodMiles: Any?,
    val recipe: Any?,
    val ingredientGraph: Any?,
    val recipeGraph: Any?,
    val processing: Any?
)