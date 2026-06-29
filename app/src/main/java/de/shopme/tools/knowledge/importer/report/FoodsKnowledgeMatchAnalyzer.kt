package de.shopme.tools.knowledge.foods.importer.report

import de.shopme.tools.knowledge.catalog.CatalogNutritionReferenceParser
import de.shopme.tools.knowledge.foods.FoodsKnowledge
import de.shopme.tools.knowledge.importer.OFFFoodsKnowledgeImportResult

class FoodsKnowledgeMatchAnalyzer(
    private val referenceParser: CatalogNutritionReferenceParser =
        CatalogNutritionReferenceParser()
) {

    fun analyze(
        incoming: FoodsKnowledge,
        matched: FoodsKnowledge,
        importResult: OFFFoodsKnowledgeImportResult
    ): FoodsKnowledgeMatchReport {

        val matchedCanonicalIds =
            matched.foods
                .map { it.id }
                .toSet()

        val matchedIncomingIds =
            matched.foods
                .mapNotNull { food ->

                    food.knowledge
                        .nutrition
                        ?.reference
                }
                .toSet()

        val unmatched =
            incoming.foods
                .filterNot { food ->

                    val mappedReference =
                        referenceParser.map(
                            food.id
                        )

                    food.id in matchedIncomingIds ||
                            mappedReference in matchedCanonicalIds
                }

        val unmatchedNames =
            unmatched
                .map { food ->

                    food.names.canonical
                }
                .groupingBy { name ->

                    name
                }
                .eachCount()
                .toList()
                .sortedWith(
                    compareByDescending<Pair<String, Int>> { entry ->

                        entry.second
                    }.thenBy { entry ->

                        entry.first
                    }
                )
                .toMap()

        val unmatchedOccurrences =
            unmatchedNames
                .keys
                .associateWith { name ->

                    importResult.nameCounts[name] ?: 0
                }
                .toList()
                .sortedWith(
                    compareByDescending<Pair<String, Int>> { entry ->

                        entry.second
                    }.thenBy { entry ->

                        entry.first
                    }
                )
                .toMap()

        return FoodsKnowledgeMatchReport(
            incomingFoods = incoming.foods.size,
            matchedFoods = matchedIncomingIds.size,
            unmatchedFoods = unmatched.size,
            unmatchedNames = unmatchedNames,
            unmatchedOccurrences = unmatchedOccurrences
        )
    }
}