package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.coverage

import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildSnapshot
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeSnapshotReader
import de.shopme.tools.knowledge.rebuild.nutrition.coverage.NutritionCoverageGapClassifier
import de.shopme.tools.knowledge.rebuild.nutrition.coverage.NutritionCoverageGapType
import de.shopme.tools.knowledge.rebuild.nutrition.coverage.NutritionNoMatchCause
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionCoverageGapClassifierTest {

    @Test
    fun classifyEveryMissingNutritionCatalogKeyExactlyOnce() {

        val directory =
            Files.createTempDirectory(
                "nutrition-coverage-gap-classifier"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )

            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
                )

            val catalogServerMappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )

            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )

            val sourceAvailabilityFile =
                File(
                    directory,
                    "nutrition.off-availability-for-no-candidates.json"
                )

            catalogFile.writeText(
                """
                {
                  "foods": [
                    {
                      "normalizedEnglish": "apple"
                    },
                    {
                      "normalizedEnglish": "banana"
                    },
                    {
                      "normalizedEnglish": "chervil"
                    },
                    {
                      "normalizedEnglish": "frozen berry mix"
                    },
                    {
                      "normalizedEnglish": "plain yogurt"
                    }
                  ]
                }
                """.trimIndent()
            )

            exactMappingFile.writeText(
                """
                {
                  "version": 1,
                  "mappings": [
                    {
                      "catalogKey": "apple",
                      "serverKey": "apple",
                      "serverArtifact": "nutrition.json"
                    }
                  ]
                }
                """.trimIndent()
            )

            catalogServerMappingFile.writeText(
                """
                {
                  "version": 1,
                  "mappings": [
                    {
                      "catalogKey": "banana",
                      "serverKey": "banana raw",
                      "sourceArtifact": "nutrition.json",
                      "method": "AI_VALIDATED",
                      "confidence": 0.95,
                      "reason": "Validated representative mapping."
                    }
                  ]
                }
                """.trimIndent()
            )

            requestFile.writeText(
                """
                {
                  "version": 1,
                  "requests": [
                    {
                      "catalogKey": "frozen berry mix",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "berry",
                          "diagnosticScore": 0.42,
                          "sharedTokens": [
                            "berry"
                          ]
                        }
                      ]
                    },
                    {
                      "catalogKey": "plain yogurt",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "plain lowfat yogurt",
                          "diagnosticScore": 0.91,
                          "sharedTokens": [
                            "plain",
                            "yogurt"
                          ]
                        }
                      ]
                    }
                  ]
                }
                """.trimIndent()
            )

            decisionFile.writeText(
                """
                {
                  "version": 1,
                  "decisions": [
                    {
                      "catalogKey": "frozen berry mix",
                      "serverArtifact": "nutrition.json",
                      "type": "NO_MATCH",
                      "selectedServerKey": null,
                      "confidence": 0.91,
                      "reason": "Candidate is not sufficiently equivalent.",
                      "decisionSource": "CHAT_GPT"
                    },
                    {
                      "catalogKey": "plain yogurt",
                      "serverArtifact": "nutrition.json",
                      "type": "MATCH",
                      "selectedServerKey": "plain lowfat yogurt",
                      "confidence": 0.91,
                      "reason": "Representative nutrition match.",
                      "decisionSource": "CHAT_GPT"
                    }
                  ]
                }
                """.trimIndent()
            )

            sourceAvailabilityFile.writeText(
                """
                {
                  "version": 1,
                  "sourceDirectory": "../data/generated/openfoodfacts",
                  "sourceFileCount": 1,
                  "scannedProductCount": 4591866,
                  "targetCount": 1,
                  "entries": [
                    {
                      "catalogKey": "chervil",
                      "aliases": [
                        "chervil",
                        "kerbel"
                      ],
                      "matchingOffProductCount": 27,
                      "directProductMatchCount": 27,
                      "ingredientOnlyMatchCount": 0,
                      "productsWithAnyNutritionCount": 0,
                      "productsWithCompleteNutritionCount": 0,
                      "productsWithoutNutritionCount": 27,
                      "productsWithIncompleteCoreNutritionCount": 0,
                      "estimatedExtractorEligibleCount": 0,
                      "estimatedExtractorRejectedCount": 27,
                      "countsByAvailabilityReason": {
                        "NO_NUTRITION_VALUES": 27
                      },
                      "samples": []
                    }
                  ]
                }
                """.trimIndent()
            )

            val snapshotReader =
                object :
                    NutritionKnowledgeSnapshotReader {

                    override fun read():
                            NutritionKnowledgeRebuildSnapshot {

                        return NutritionKnowledgeRebuildSnapshot(
                            mappingCount =
                                1,
                            catalogItemCount =
                                5,
                            exactMatchCount =
                                1,
                            mappedMatchCount =
                                1,
                            runtimeEntryCount =
                                2,
                            coveredCatalogItemCount =
                                2,
                            missingCatalogItemCount =
                                3,
                            coverage =
                                0.4
                        )
                    }
                }

            val report =
                NutritionCoverageGapClassifier(
                    catalogFile =
                        catalogFile,
                    exactMappingFile =
                        exactMappingFile,
                    catalogServerMappingFile =
                        catalogServerMappingFile,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    sourceAvailabilityFile =
                        sourceAvailabilityFile,
                    snapshotReader =
                        snapshotReader
                )
                    .classify()

            assertEquals(
                expected =
                    5,
                actual =
                    report.catalogItemCount
            )

            assertEquals(
                expected =
                    2,
                actual =
                    report.coveredCatalogItemCount
            )

            assertEquals(
                expected =
                    3,
                actual =
                    report.missingCatalogItemCount
            )

            assertEquals(
                expected =
                    3,
                actual =
                    report.classifiedGapCount
            )

            assertEquals(
                expected =
                    0,
                actual =
                    report.unclassifiedGapCount
            )

            assertEquals(
                expected =
                    listOf(
                        "chervil",
                        "frozen berry mix",
                        "plain yogurt"
                    ),
                actual =
                    report.gaps.map {
                        it.catalogKey
                    }
            )

            assertEquals(
                expected =
                    NutritionCoverageGapType
                        .SOURCE_DATA_NO_NUTRITION,
                actual =
                    report.gaps
                        .first {
                            it.catalogKey ==
                                    "chervil"
                        }
                        .type
            )

            assertEquals(
                expected =
                    NutritionCoverageGapType.VERY_LOW_SCORE,
                actual =
                    report.gaps
                        .first {
                            it.catalogKey ==
                                    "frozen berry mix"
                        }
                        .type
            )

            assertEquals(
                expected =
                    NutritionCoverageGapType.MATCH_NOT_PERSISTED,
                actual =
                    report.gaps
                        .first {
                            it.catalogKey ==
                                    "plain yogurt"
                        }
                        .type
            )

        } finally {

            directory.deleteRecursively()
        }
    }

    @Test
    fun preferSpecificSemanticCauseOverScoreCluster() {

        val directory =
            Files.createTempDirectory(
                "nutrition-coverage-gap-semantic-priority"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )

            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
                )

            val catalogServerMappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )

            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )

            catalogFile.writeText(
                """
            {
              "foods": [
                {
                  "normalizedEnglish": "acerola juice"
                }
              ]
            }
            """.trimIndent()
            )

            exactMappingFile.writeText(
                """
            {
              "version": 1,
              "mappings": []
            }
            """.trimIndent()
            )

            catalogServerMappingFile.writeText(
                """
            {
              "version": 1,
              "mappings": []
            }
            """.trimIndent()
            )

            requestFile.writeText(
                """
            {
              "version": 1,
              "requests": [
                {
                  "catalogKey": "acerola juice",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "orange mango acerola juice drink",
                      "diagnosticScore": 0.80,
                      "sharedTokens": [
                        "acerola",
                        "juice"
                      ]
                    },
                    {
                      "serverKey": "mango acerola juice beverage",
                      "diagnosticScore": 0.78,
                      "sharedTokens": [
                        "acerola",
                        "juice"
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
            )

            decisionFile.writeText(
                """
            {
              "version": 1,
              "decisions": [
                {
                  "catalogKey": "acerola juice",
                  "serverArtifact": "nutrition.json",
                  "type": "NO_MATCH",
                  "selectedServerKey": null,
                  "confidence": 0.93,
                  "reason": "Candidates are more specific mixed drinks.",
                  "decisionSource": "CHAT_GPT"
                }
              ]
            }
            """.trimIndent()
            )

            val snapshotReader =
                object :
                    NutritionKnowledgeSnapshotReader {

                    override fun read():
                            NutritionKnowledgeRebuildSnapshot {

                        return NutritionKnowledgeRebuildSnapshot(
                            mappingCount =
                                0,
                            catalogItemCount =
                                1,
                            exactMatchCount =
                                0,
                            mappedMatchCount =
                                0,
                            runtimeEntryCount =
                                0,
                            coveredCatalogItemCount =
                                0,
                            missingCatalogItemCount =
                                1,
                            coverage =
                                0.0
                        )
                    }
                }

            val report =
                NutritionCoverageGapClassifier(
                    catalogFile =
                        catalogFile,
                    exactMappingFile =
                        exactMappingFile,
                    catalogServerMappingFile =
                        catalogServerMappingFile,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    snapshotReader =
                        snapshotReader
                )
                    .classify()

            val gap =
                report.gaps.single()

            assertEquals(
                expected =
                    NutritionCoverageGapType.TOO_SPECIFIC,
                actual =
                    gap.type
            )

        } finally {

            directory.deleteRecursively()
        }
    }

    @Test
    fun classifyResidualNoMatchCauseDeterministically() {

        val directory =
            Files.createTempDirectory(
                "nutrition-no-match-cause"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )

            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
                )

            val catalogServerMappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )

            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )

            catalogFile.writeText(
                """
            {
              "foods": [
                {
                  "normalizedEnglish": "apple juice"
                }
              ]
            }
            """.trimIndent()
            )

            exactMappingFile.writeText(
                """
            {
              "version": 1,
              "mappings": []
            }
            """.trimIndent()
            )

            catalogServerMappingFile.writeText(
                """
            {
              "version": 1,
              "mappings": []
            }
            """.trimIndent()
            )

            requestFile.writeText(
                """
            {
              "version": 1,
              "requests": [
                {
                  "catalogKey": "apple juice",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "apple drink",
                      "diagnosticScore": 0.74,
                      "sharedTokens": [
                        "apple"
                      ]
                    },
                    {
                      "serverKey": "apple beverage",
                      "diagnosticScore": 0.68,
                      "sharedTokens": [
                        "apple"
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
            )

            decisionFile.writeText(
                """
            {
              "version": 1,
              "decisions": [
                {
                  "catalogKey": "apple juice",
                  "serverArtifact": "nutrition.json",
                  "type": "NO_MATCH",
                  "selectedServerKey": null,
                  "confidence": 0.90,
                  "reason": "The candidate is broader than apple juice.",
                  "decisionSource": "CHAT_GPT"
                }
              ]
            }
            """.trimIndent()
            )

            val snapshotReader =
                object :
                    NutritionKnowledgeSnapshotReader {

                    override fun read():
                            NutritionKnowledgeRebuildSnapshot {

                        return NutritionKnowledgeRebuildSnapshot(
                            mappingCount =
                                0,
                            catalogItemCount =
                                1,
                            exactMatchCount =
                                0,
                            mappedMatchCount =
                                0,
                            runtimeEntryCount =
                                0,
                            coveredCatalogItemCount =
                                0,
                            missingCatalogItemCount =
                                1,
                            coverage =
                                0.0
                        )
                    }
                }

            val report =
                NutritionCoverageGapClassifier(
                    catalogFile =
                        catalogFile,
                    exactMappingFile =
                        exactMappingFile,
                    catalogServerMappingFile =
                        catalogServerMappingFile,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    snapshotReader =
                        snapshotReader
                )
                    .classify()

            val gap =
                report.gaps.single()

            assertEquals(
                expected =
                    NutritionCoverageGapType.NO_MATCH,
                actual =
                    gap.type
            )

            assertEquals(
                expected =
                    NutritionNoMatchCause.MODERATE_TOP_CANDIDATE_REJECTED,
                actual =
                    gap.noMatchCause
            )

        } finally {

            directory.deleteRecursively()
        }
    }
}