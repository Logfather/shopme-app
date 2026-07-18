package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.CatalogServerKnowledgeMapping
import de.shopme.tools.knowledge.mapping.catalog.CatalogServerKnowledgeMappingMerger
import de.shopme.tools.knowledge.mapping.catalog.CatalogServerKnowledgeMappingMethod
import de.shopme.tools.knowledge.mapping.catalog.CatalogServerKnowledgeMappings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CatalogServerKnowledgeMappingMergerTest {

    @Test
    fun mergesMappingsWithoutOverwritingConflicts() {

        val existing =
            mappings(
                mapping(
                    catalogKey =
                        "apple yogurt",
                    serverKey =
                        "apple yogurt",
                    sourceArtifact =
                        "nutrition.json",
                    confidence =
                        1.0,
                    method =
                        CatalogServerKnowledgeMappingMethod.EXACT
                ),
                mapping(
                    catalogKey =
                        "matjes herring in cream sauce",
                    serverKey =
                        "herring fillets in cream sauce",
                    sourceArtifact =
                        "nutrition.json",
                    confidence =
                        0.93
                )
            )

        val nutritionIncoming =
            mappings(
                mapping(
                    catalogKey =
                        "matjes herring in cream sauce",
                    serverKey =
                        "herring fillets in cream sauce",
                    sourceArtifact =
                        "nutrition.json",
                    confidence =
                        0.93
                ),
                mapping(
                    catalogKey =
                        "rice yogurt",
                    serverKey =
                        "light rice yogurt",
                    sourceArtifact =
                        "nutrition.json",
                    confidence =
                        0.84
                )
            )

        val ingredientsIncoming =
            mappings(
                mapping(
                    catalogKey =
                        "apple yogurt",
                    serverKey =
                        "apple yogurt light",
                    sourceArtifact =
                        "ingredients.json",
                    confidence =
                        0.91
                ),
                mapping(
                    catalogKey =
                        "rice yogurt",
                    serverKey =
                        "light rice yogurt",
                    sourceArtifact =
                        "ingredients.json",
                    confidence =
                        0.89
                ),
                mapping(
                    catalogKey =
                        "zucchini spaghetti",
                    serverKey =
                        "zucchini noodles",
                    sourceArtifact =
                        "ingredients.json",
                    confidence =
                        0.90
                )
            )

        val result =
            CatalogServerKnowledgeMappingMerger()
                .merge(
                    existing =
                        existing,
                    incoming =
                        listOf(
                            nutritionIncoming,
                            ingredientsIncoming
                        )
                )

        assertEquals(
            4,
            result.mappings.mappings.size
        )

        val byCatalogKey =
            result.mappings.mappings
                .associateBy {
                    it.catalogKey
                }

        assertEquals(
            "apple yogurt",
            byCatalogKey
                .getValue(
                    "apple yogurt"
                )
                .serverKey
        )

        assertEquals(
            "herring fillets in cream sauce",
            byCatalogKey
                .getValue(
                    "matjes herring in cream sauce"
                )
                .serverKey
        )

        /*
         * Beide eingehenden Dateien liefern denselben Server-Key.
         * Es wird deterministisch das Mapping mit höherer Confidence
         * als Repräsentant gewählt.
         */
        val riceMapping =
            byCatalogKey.getValue(
                "rice yogurt"
            )

        assertEquals(
            "light rice yogurt",
            riceMapping.serverKey
        )

        assertEquals(
            "ingredients.json",
            riceMapping.sourceArtifact
        )

        assertEquals(
            0.89,
            riceMapping.confidence
        )

        assertEquals(
            "zucchini noodles",
            byCatalogKey
                .getValue(
                    "zucchini spaghetti"
                )
                .serverKey
        )

        assertEquals(
            2,
            result.report.existingMappingCount
        )

        assertEquals(
            5,
            result.report.incomingMappingCount
        )

        assertEquals(
            2,
            result.report.addedMappingCount
        )

        assertEquals(
            2,
            result.report.unchangedMappingCount
        )

        assertEquals(
            1,
            result.report.conflictCount
        )

        assertEquals(
            4,
            result.report.totalMappingCount
        )

        val conflict =
            result.report.conflicts.single()

        assertEquals(
            "apple yogurt",
            conflict.catalogKey
        )

        assertEquals(
            "apple yogurt",
            conflict.retainedServerKey
        )

        assertEquals(
            "apple yogurt light",
            conflict.conflictingServerKey
        )

        assertEquals(
            "nutrition.json",
            conflict.retainedSourceArtifact
        )

        assertEquals(
            "ingredients.json",
            conflict.conflictingSourceArtifact
        )
    }


    @Test
    fun rejectsConflictingNewMappingsWithoutChoosingOne() {

        val first =
            mappings(
                mapping(
                    catalogKey =
                        "vegetable lasagna",
                    serverKey =
                        "vegetable lasagna, vegetable",
                    sourceArtifact =
                        "nutrition.json",
                    confidence =
                        0.90
                )
            )

        val second =
            mappings(
                mapping(
                    catalogKey =
                        "vegetable lasagna",
                    serverKey =
                        "lasagna with vegetables",
                    sourceArtifact =
                        "ingredients.json",
                    confidence =
                        0.95
                )
            )

        val result =
            CatalogServerKnowledgeMappingMerger()
                .merge(
                    existing =
                        mappings(),
                    incoming =
                        listOf(
                            first,
                            second
                        )
                )

        assertNull(
            result.mappings.mappings
                .singleOrNull {
                    it.catalogKey ==
                            "vegetable lasagna"
                }
        )

        assertEquals(
            0,
            result.report.addedMappingCount
        )

        assertEquals(
            1,
            result.report.conflictCount
        )
    }


    @Test
    fun resultDoesNotDependOnIncomingFileOrder() {

        val nutrition =
            mappings(
                mapping(
                    catalogKey =
                        "rice yogurt",
                    serverKey =
                        "light rice yogurt",
                    sourceArtifact =
                        "nutrition.json",
                    confidence =
                        0.84
                )
            )

        val ingredients =
            mappings(
                mapping(
                    catalogKey =
                        "rice yogurt",
                    serverKey =
                        "light rice yogurt",
                    sourceArtifact =
                        "ingredients.json",
                    confidence =
                        0.89
                )
            )

        val merger =
            CatalogServerKnowledgeMappingMerger()

        val firstResult =
            merger.merge(
                existing =
                    mappings(),
                incoming =
                    listOf(
                        nutrition,
                        ingredients
                    )
            )

        val secondResult =
            merger.merge(
                existing =
                    mappings(),
                incoming =
                    listOf(
                        ingredients,
                        nutrition
                    )
            )

        assertEquals(
            firstResult,
            secondResult
        )
    }


    private fun mappings(
        vararg mappings:
        CatalogServerKnowledgeMapping
    ): CatalogServerKnowledgeMappings =
        CatalogServerKnowledgeMappings(
            version =
                CatalogServerKnowledgeMappings.CURRENT_VERSION,
            mappings =
                mappings
                    .toList()
                    .sortedWith(
                        CatalogServerKnowledgeMappings.MAPPING_ORDER
                    )
        )


    private fun mapping(
        catalogKey: String,
        serverKey: String,
        sourceArtifact: String,
        confidence: Double,
        method:
        CatalogServerKnowledgeMappingMethod =
            CatalogServerKnowledgeMappingMethod.AI_VALIDATED
    ): CatalogServerKnowledgeMapping =
        CatalogServerKnowledgeMapping(
            catalogKey =
                catalogKey,
            serverKey =
                serverKey,
            sourceArtifact =
                sourceArtifact,
            method =
                method,
            confidence =
                confidence,
            reason =
                "Recorded merger test mapping"
        )
}