package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.gap.CatalogKnowledgeGap

class OFFKnowledgeCandidateBuilder {

    fun build(
        gaps: List<CatalogKnowledgeGap>,
        extracts: List<OFFHivraExtract>
    ): List<OFFKnowledgeCandidate> {

        val gapsByName =
            gaps.associateBy {
                it.normalizedName
            }

        return extracts
            .mapNotNull { extract ->

                val normalizedName =
                    normalize(
                        extract.productName
                    )

                val gap =
                    gapsByName[normalizedName]
                        ?: return@mapNotNull null

                val dimensions =
                    gap
                        .missingDimensions
                        .filter {
                            it in extract.availableDimensions
                        }
                        .toSet()

                if (dimensions.isEmpty()) {
                    return@mapNotNull null
                }

                OFFKnowledgeCandidate(
                    catalogNormalizedName =
                        gap.normalizedName,

                    offCode =
                        extract.code,

                    offProductName =
                        extract.productName,

                    source =
                        "open_food_facts",

                    dimensions =
                        dimensions,

                    nutritionFacts =
                        null
                )
            }
    }

    private fun normalize(
        value: String
    ): String {

        return value
            .lowercase()
            .trim()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
    }
}