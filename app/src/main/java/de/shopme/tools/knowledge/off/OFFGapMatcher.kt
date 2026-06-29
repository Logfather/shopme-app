package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.gap.CatalogKnowledgeGap

class OFFGapMatcher {

    fun match(
        gaps: List<CatalogKnowledgeGap>,
        products: List<OFFProductCandidate>
    ): List<OFFGapMatch> {

        return gaps.mapNotNull { gap ->

            val product =
                products.firstOrNull {
                    it.normalizedName == gap.normalizedName
                }

            if (product == null) {
                null
            } else {
                OFFGapMatch(
                    catalogFood = gap.normalizedName,
                    offProductName = product.productName,
                    offId = product.id,
                    matchedDimensions = findMatchedDimensions(
                        gap = gap,
                        product = product
                    )
                )
            }
        }.filter {
            it.matchedDimensions.isNotEmpty()
        }
    }

    private fun findMatchedDimensions(
        gap: CatalogKnowledgeGap,
        product: OFFProductCandidate
    ): Set<KnowledgeDimensionId> {

        val result =
            mutableSetOf<KnowledgeDimensionId>()

        if (
            KnowledgeDimensionId.NUTRITION in gap.missingDimensions &&
            product.hasNutritionFacts
        ) {
            result.add(KnowledgeDimensionId.NUTRITION)
        }

        if (
            KnowledgeDimensionId.ALLERGENS in gap.missingDimensions &&
            product.hasAllergens
        ) {
            result.add(KnowledgeDimensionId.ALLERGENS)
        }

        return result
    }
}