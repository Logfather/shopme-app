package de.shopme.tools.knowledge.foods.importer.report

import de.shopme.tools.knowledge.catalog.CatalogNutritionReferenceParser
import de.shopme.tools.knowledge.foods.FoodsKnowledge

class FoodsKnowledgeExpansionCandidateAnalyzer(
    private val referenceParser: CatalogNutritionReferenceParser =
        CatalogNutritionReferenceParser()
) {

    fun analyze(
        canonical: FoodsKnowledge,
        matchReport: FoodsKnowledgeMatchReport,
        limit: Int = 50
    ): List<FoodsKnowledgeExpansionCandidate> {

        val canonicalIds =
            canonical.foods
                .map {
                    it.id
                }
                .toSet()

        return matchReport.unmatchedOccurrences
            .entries
            .asSequence()
            .filterNot { entry ->

                val mappedReference =
                    referenceParser.map(
                        entry.key
                    )

                mappedReference in canonicalIds
            }
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> {
                    it.value
                }.thenBy {
                    it.key
                }
            )
            .take(limit)
            .map { entry ->

            val mappedReference =
                referenceParser.map(
                    entry.key
                )

            FoodsKnowledgeExpansionCandidate(
                name = entry.key,
                mappedReference = mappedReference,
                canonicalExists = mappedReference in canonicalIds,
                occurrences = entry.value
            )
        }
            .toList()
    }
}