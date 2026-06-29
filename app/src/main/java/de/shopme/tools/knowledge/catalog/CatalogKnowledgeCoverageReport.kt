package de.shopme.tools.knowledge.catalog

data class CatalogKnowledgeCoverageReport(

    val totalItems: Int,

    val entries: List<CatalogKnowledgeCoverageEntry>

) {

    val itemsWithNutrition: Int =
        entries.count {
            it.hasNutrition
        }

    val itemsWithoutNutrition: Int =
        totalItems - itemsWithNutrition

    fun missingNutrition(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasNutrition
        }

    fun missingAllergens(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasAllergens
        }

    fun missingIngredients(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasIngredients
        }

    fun missingTaxonomy(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasTaxonomy
        }

    fun missingSeasonality(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasSeasonality
        }

    fun missingProduction(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasProduction
        }

    fun missingProcessing(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasProcessing
        }

    fun missingPackaging(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasPackaging
        }

    fun missingCarbon(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasCarbon
        }

    fun missingWater(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasWater
        }

    fun missingWaterStress(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasWaterStress
        }

    fun missingBiodiversity(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasBiodiversity
        }

    fun missingPollinator(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasPollinator
        }

    fun missingPesticides(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasPesticides
        }

    fun missingAnimalWelfare(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasAnimalWelfare
        }

    fun missingFairTrade(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasFairTrade
        }

    fun missingFoodMiles(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasFoodMiles
        }

    fun missingLocality(): List<CatalogKnowledgeCoverageEntry> =
        entries.filter {
            !it.hasLocality
        }
}