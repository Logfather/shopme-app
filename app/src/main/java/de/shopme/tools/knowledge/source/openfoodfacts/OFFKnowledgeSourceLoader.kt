package de.shopme.tools.knowledge.source.openfoodfacts

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId
import de.shopme.tools.knowledge.off.OFFKnowledgeCandidate
import de.shopme.tools.knowledge.off.OFFProductCandidateLoader
import java.io.File

class OFFKnowledgeSourceLoader(
    private val productCandidateLoader: OFFProductCandidateLoader
) {

    fun load(
        input: File
    ): List<OFFKnowledgeCandidate> {

        return productCandidateLoader
            .load(input)
            .mapNotNull { productCandidate ->

                val dimensions =
                    buildSet {

                        if (productCandidate.hasNutritionFacts) {
                            add(KnowledgeDimensionId.NUTRITION)
                        }

                        if (productCandidate.hasAllergens) {
                            add(KnowledgeDimensionId.ALLERGENS)
                        }
                    }

                if (dimensions.isEmpty()) {
                    return@mapNotNull null
                }

                OFFKnowledgeCandidate(
                    catalogNormalizedName = productCandidate.normalizedName,
                    offCode = productCandidate.id,
                    offProductName = productCandidate.productName,
                    source = "open_food_facts",
                    dimensions = dimensions
                )
            }
    }
}