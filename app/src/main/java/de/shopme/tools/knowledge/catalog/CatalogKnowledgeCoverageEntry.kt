package de.shopme.tools.knowledge.catalog

data class CatalogKnowledgeCoverageEntry(

    val itemName: String,

    val normalized: String,

    val hasNutrition: Boolean,

    val hasAllergens: Boolean,

    val hasIngredients: Boolean,

    val hasTaxonomy: Boolean,

    val hasSeasonality: Boolean,

    val hasProduction: Boolean,

    val hasProcessing: Boolean,

    val hasPackaging: Boolean,

    val hasCarbon: Boolean,

    val hasWater: Boolean,

    val hasWaterStress: Boolean,

    val hasBiodiversity: Boolean,

    val hasPollinator: Boolean,

    val hasPesticides: Boolean,

    val hasAnimalWelfare: Boolean,

    val hasFairTrade: Boolean,

    val hasFoodMiles: Boolean,

    val hasLocality: Boolean

)