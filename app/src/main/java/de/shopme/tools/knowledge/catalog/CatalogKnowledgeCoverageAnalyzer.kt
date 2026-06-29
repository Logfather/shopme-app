package de.shopme.tools.knowledge.catalog

import de.shopme.domain.catalog.CatalogItem

class CatalogKnowledgeCoverageAnalyzer {

    fun analyze(
        catalog: List<CatalogItem>
    ): CatalogKnowledgeCoverageReport {

        val entries =
            catalog.map { item ->

                CatalogKnowledgeCoverageEntry(
                    itemName = item.itemname,
                    normalized = item.normalized,

                    hasNutrition =
                        item.knowledge?.nutrition?.reference
                            .isPresent(),

                    hasAllergens =
                        item.knowledge?.allergens?.reference
                            .isPresent(),

                    hasIngredients =
                        item.knowledge?.ingredients?.reference
                            .isPresent(),

                    hasTaxonomy =
                        item.knowledge?.taxonomy?.reference
                            .isPresent(),

                    hasSeasonality =
                        item.knowledge?.seasonality?.reference
                            .isPresent(),

                    hasProduction =
                        item.knowledge?.production?.reference
                            .isPresent(),

                    hasProcessing =
                        item.knowledge?.processing?.reference
                            .isPresent(),

                    hasPackaging =
                        item.knowledge?.packaging?.reference
                            .isPresent(),

                    hasCarbon =
                        item.knowledge?.carbon?.reference
                            .isPresent(),

                    hasWater =
                        item.knowledge?.water?.reference
                            .isPresent(),

                    hasWaterStress =
                        item.knowledge?.waterStress?.reference
                            .isPresent(),

                    hasBiodiversity =
                        item.knowledge?.biodiversity?.reference
                            .isPresent(),

                    hasPollinator =
                        item.knowledge?.pollinator?.reference
                            .isPresent(),

                    hasPesticides =
                        item.knowledge?.pesticides?.reference
                            .isPresent(),

                    hasAnimalWelfare =
                        item.knowledge?.animalWelfare?.reference
                            .isPresent(),

                    hasFairTrade =
                        item.knowledge?.fairTrade?.reference
                            .isPresent(),

                    hasFoodMiles =
                        item.knowledge?.foodMiles?.reference
                            .isPresent(),

                    hasLocality =
                        item.knowledge?.locality?.reference
                            .isPresent()
                )
            }

        return CatalogKnowledgeCoverageReport(
            totalItems = catalog.size,
            entries = entries
        )
    }

    private fun String?.isPresent(): Boolean =
        !this.isNullOrBlank()
}