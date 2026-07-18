package de.shopme.tools.knowledge.ai.builder.off

data class OFFCandidateData(
    val sourceId: String,
    val name: String?,

    val nutrition: Any?,

    val ingredients: List<String>,
    val taxonomy: List<String>,
    val allergens: List<String>,
    val packaging: List<String>,
    val production: List<String>,
    val locality: List<String>,
    val processing: List<String>,

    val glycemic: Double?,
    val water: Double?,
    val carbon: Double?,
    val waterStress: Double?,
    val biodiversity: Double?,
    val pollinator: Double?,
    val fairtrade: Boolean?,
    val animalWelfare: String?,
    val seasonality: String?,
    val foodMiles: Double?,

    val recipe: Any?,
    val ingredientGraph: Any?,
    val recipeGraph: Any?
)