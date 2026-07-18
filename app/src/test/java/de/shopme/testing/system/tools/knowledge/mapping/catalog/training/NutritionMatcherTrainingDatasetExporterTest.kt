package de.shopme.testing.system.tools.knowledge.mapping.catalog.training

import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingDatasetExporter
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingExampleRole
import de.shopme.tools.knowledge.mapping.catalog.training.NutritionMatcherTrainingLabel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NutritionMatcherTrainingDatasetExporterTest {

    @Test
    fun exportPositiveAndNegativeExamplesDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-matcher-training-"
            )
                .toFile()

        try {
            val candidateQualityFile =
                File(
                    directory,
                    "nutrition.rejected-candidate-quality.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "entries": [
                                {
                                  "catalogKey": "fruit yogurt",
                                  "selectedCandidateRank": 2,
                                  "candidates": [
                                    {
                                      "serverKey": "plain yogurt",
                                      "diagnosticScore": 0.61,
                                      "sharedTokens": [
                                        "yogurt"
                                      ],
                                      "selected": false
                                    },
                                    {
                                      "serverKey": "cherry fruit yogurt",
                                      "diagnosticScore": 0.79,
                                      "sharedTokens": [
                                        "fruit",
                                        "yogurt"
                                      ],
                                      "selected": true
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "fish sausage",
                                  "selectedCandidateRank": null,
                                  "candidates": [
                                    {
                                      "serverKey": "pork sausage",
                                      "diagnosticScore": 0.68,
                                      "sharedTokens": [
                                        "sausage"
                                      ],
                                      "selected": false
                                    }
                                  ]
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val diagnosticsFile =
                File(
                    directory,
                    "nutrition.match-diagnostics.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "diagnostics": [
                                {
                                  "catalogKey": "fruit yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 2,
                                  "candidateServerKeys": [
                                    "plain yogurt",
                                    "cherry fruit yogurt"
                                  ],
                                  "decisionType": "MATCH",
                                  "selectedServerKey": "cherry fruit yogurt",
                                  "confidence": 0.78,
                                  "decisionReason": "Representative yogurt.",
                                  "validationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "validationReason": "Below threshold.",
                                  "mappingWritten": false
                                },
                                {
                                  "catalogKey": "fish sausage",
                                  "candidateCount": 1,
                                  "candidateServerKeys": [
                                    "pork sausage"
                                  ],
                                  "decisionType": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 0.92,
                                  "decisionReason": "Different meat class.",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "validationReason": "AI returned NO_MATCH.",
                                  "mappingWritten": false
                                },
                                {
                                  "catalogKey": "apple",
                                  "candidateCount": 2,
                                  "candidateServerKeys": [
                                    "apple raw",
                                    "apple pie"
                                  ],
                                  "decisionType": "MATCH",
                                  "selectedServerKey": "apple raw",
                                  "confidence": 0.96,
                                  "decisionReason": "Same product.",
                                  "validationStatus": "ACCEPTED",
                                  "validationReason": "Accepted.",
                                  "mappingWritten": true
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val representativeValidationFile =
                File(
                    directory,
                    "nutrition.low-confidence-validation.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "entries": [
                                {
                                  "catalogKey": "fruit yogurt",
                                  "selectedServerKey": "cherry fruit yogurt",
                                  "candidateRank": 2,
                                  "originalConfidence": 0.78,
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "decisionType": "REPRESENTATIVE",
                                  "reasons": [
                                    "SAME_PRODUCT_CLASS"
                                  ],
                                  "accepted": true
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val mappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "mappings": [
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "apple raw"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "nutrition.matcher-training-dataset.json"
                )

            val exporter =
                NutritionMatcherTrainingDatasetExporter()

            val first =
                exporter.run(
                    candidateQualityFile =
                        candidateQualityFile,
                    diagnosticsFile =
                        diagnosticsFile,
                    representativeValidationFile =
                        representativeValidationFile,
                    mappingFile =
                        mappingFile,
                    outputFile =
                        outputFile,
                    output =
                        PrintStream(
                            ByteArrayOutputStream()
                        )
                )

            val firstContent =
                outputFile.readText()

            val second =
                exporter.run(
                    candidateQualityFile =
                        candidateQualityFile,
                    diagnosticsFile =
                        diagnosticsFile,
                    representativeValidationFile =
                        representativeValidationFile,
                    mappingFile =
                        mappingFile,
                    outputFile =
                        outputFile,
                    output =
                        PrintStream(
                            ByteArrayOutputStream()
                        )
                )

            assertEquals(
                expected =
                    first.dataset,
                actual =
                    second.dataset
            )

            assertEquals(
                expected =
                    firstContent,
                actual =
                    outputFile.readText()
            )

            assertEquals(
                expected = 3,
                actual =
                    first.dataset.summary.sourceCatalogKeyCount
            )

            assertEquals(
                expected = 4,
                actual =
                    first.dataset.summary.exampleCount
            )

            assertEquals(
                expected = 2,
                actual =
                    first.dataset.summary.positiveCount
            )

            assertEquals(
                expected = 2,
                actual =
                    first.dataset.summary.negativeCount
            )

            assertEquals(
                expected = 1,
                actual =
                    first.dataset.summary.acceptedOriginalMatchCount
            )

            assertEquals(
                expected = 1,
                actual =
                    first.dataset.summary.acceptedSelectedCount
            )

            assertEquals(
                expected = 0,
                actual =
                    first.dataset.summary.rejectedSelectedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    first.dataset.summary.rejectedNoMatchCandidateCount
            )

            assertEquals(
                expected = 1,
                actual =
                    first.dataset.summary.nonSelectedAlternativeCount
            )

            val acceptedOriginal =
                first.dataset.examples.single {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_ORIGINAL_MATCH
                }

            assertEquals(
                expected =
                    NutritionMatcherTrainingLabel.POSITIVE,
                actual =
                    acceptedOriginal.label
            )

            assertEquals(
                expected =
                    "apple",
                actual =
                    acceptedOriginal.catalogKey
            )

            assertEquals(
                expected =
                    "apple raw",
                actual =
                    acceptedOriginal.serverKey
            )

            assertEquals(
                expected = 1,
                actual =
                    acceptedOriginal.candidateRank
            )

            assertEquals(
                expected = 2,
                actual =
                    acceptedOriginal.candidateCount
            )

            assertTrue(
                acceptedOriginal.selected
            )

            assertFalse(
                acceptedOriginal.diagnosticScoreAvailable
            )

            assertEquals(
                expected = 0.0,
                actual =
                    acceptedOriginal.diagnosticScore
            )

            assertEquals(
                expected =
                    listOf(
                        "apple"
                    ),
                actual =
                    acceptedOriginal.sharedTokens
            )

            assertEquals(
                expected =
                    "ACCEPTED",
                actual =
                    acceptedOriginal.originalValidationStatus
            )

            assertEquals(
                expected = null,
                actual =
                    acceptedOriginal.representativeDecisionType
            )

            assertTrue(
                acceptedOriginal.representativeReasons.isEmpty()
            )

            val acceptedRepresentative =
                first.dataset.examples.single {
                    it.role ==
                            NutritionMatcherTrainingExampleRole
                                .ACCEPTED_SELECTED
                }

            assertEquals(
                expected =
                    NutritionMatcherTrainingLabel.POSITIVE,
                actual =
                    acceptedRepresentative.label
            )

            assertEquals(
                expected =
                    "fruit yogurt",
                actual =
                    acceptedRepresentative.catalogKey
            )

            assertTrue(
                acceptedRepresentative.selected
            )

            assertTrue(
                acceptedRepresentative.diagnosticScoreAvailable
            )

            assertEquals(
                expected = 0.79,
                actual =
                    acceptedRepresentative.diagnosticScore
            )

            assertEquals(
                expected =
                    "REPRESENTATIVE",
                actual =
                    acceptedRepresentative
                        .representativeDecisionType
            )

            val alternativeNegative =
                first.dataset.examples.single {
                    it.catalogKey ==
                            "fruit yogurt" &&
                            !it.selected
                }

            assertEquals(
                expected =
                    NutritionMatcherTrainingLabel.NEGATIVE,
                actual =
                    alternativeNegative.label
            )

            assertEquals(
                expected =
                    NutritionMatcherTrainingExampleRole
                        .NON_SELECTED_ALTERNATIVE,
                actual =
                    alternativeNegative.role
            )

            assertTrue(
                alternativeNegative.diagnosticScoreAvailable
            )

            val noMatchNegative =
                first.dataset.examples.single {
                    it.catalogKey ==
                            "fish sausage"
                }

            assertEquals(
                expected =
                    NutritionMatcherTrainingLabel.NEGATIVE,
                actual =
                    noMatchNegative.label
            )

            assertEquals(
                expected =
                    NutritionMatcherTrainingExampleRole
                        .REJECTED_NO_MATCH_CANDIDATE,
                actual =
                    noMatchNegative.role
            )

            assertFalse(
                noMatchNegative.selected
            )

            assertTrue(
                noMatchNegative.diagnosticScoreAvailable
            )



        } finally {
            directory.deleteRecursively()
        }
    }
}