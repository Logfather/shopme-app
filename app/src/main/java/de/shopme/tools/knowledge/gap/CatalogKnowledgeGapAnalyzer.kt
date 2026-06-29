package de.shopme.tools.knowledge.gap

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

class CatalogKnowledgeGapAnalyzer {

    fun analyze(
        catalogItems: List<CatalogItem>
    ): List<CatalogKnowledgeGap> {

        return catalogItems
            .mapNotNull { item ->

                val missingDimensions =
                    findMissingDimensions(item)

                if (missingDimensions.isEmpty()) {
                    null
                } else {
                    CatalogKnowledgeGap(
                        normalizedName = item.normalized,
                        missingDimensions = missingDimensions
                    )
                }
            }
    }

    private fun findMissingDimensions(
        item: CatalogItem
    ): Set<KnowledgeDimensionId> {

        val missing =
            mutableSetOf<KnowledgeDimensionId>()

        if (item.nutritionKnowledgeReference().isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.NUTRITION)
        }

        if (item.knowledge?.allergens?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.ALLERGENS)
        }

        if (item.knowledge?.ingredients?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.INGREDIENTS)
        }

        if (item.knowledge?.taxonomy?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.FOOD_TAXONOMY)
        }

        if (item.knowledge?.seasonality?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.SEASONALITY)
        }

        if (item.knowledge?.production?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.PRODUCTION)
        }

        if (item.knowledge?.processing?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.PROCESSING)
        }

        if (item.knowledge?.packaging?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.PACKAGING)
        }

        if (item.knowledge?.carbon?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.CARBON)
        }

        if (item.knowledge?.water?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.WATER)
        }

        if (item.knowledge?.waterStress?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.WATER_STRESS)
        }

        if (item.knowledge?.biodiversity?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.BIODIVERSITY)
        }

        if (item.knowledge?.pollinator?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.POLLINATOR)
        }

        if (item.knowledge?.pesticides?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.PESTICIDES)
        }

        if (item.knowledge?.animalWelfare?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.ANIMAL_WELFARE)
        }

        if (item.knowledge?.fairTrade?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.FAIR_TRADE)
        }

        if (item.knowledge?.foodMiles?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.FOOD_MILES)
        }

        if (item.knowledge?.locality?.reference.isNullOrBlank()) {
            missing.add(KnowledgeDimensionId.LOCALITY)
        }

        return missing
    }
}