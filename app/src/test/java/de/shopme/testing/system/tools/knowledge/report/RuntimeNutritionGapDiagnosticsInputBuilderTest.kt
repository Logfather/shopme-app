package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.knowledge.report.RuntimeKnowledgeGapDecisionOutcome
import de.shopme.tools.knowledge.report.RuntimeKnowledgeGapInputBuilder
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RuntimeNutritionGapDiagnosticsInputBuilderTest {

    @Test
    fun readPersistedNutritionMatchDiagnostics() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-gap-diagnostics-"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )
                    .apply {
                        writeText(
                            """
                            [
                              {
                                "normalizedEnglish": "No Candidates"
                              },
                              {
                                "normalizedEnglish": "No Match"
                              },
                              {
                                "normalizedEnglish": "Low Confidence"
                              }
                            ]
                            """.trimIndent()
                        )
                    }

            val nutritionFile =
                File(
                    directory,
                    "nutrition.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "entries": {
                                "possible product": {}
                              }
                            }
                            """.trimIndent()
                        )
                    }

            val requestFile =
                File(
                    directory,
                    "requests.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "requests": [
                                {
                                  "catalogKey": "no candidates",
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "no match",
                                  "candidates": [
                                    {
                                      "serverKey": "possible product"
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "low confidence",
                                  "candidates": [
                                    {
                                      "serverKey": "possible product"
                                    }
                                  ]
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val decisionFile =
                File(
                    directory,
                    "decisions.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "decisions": []
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
                                  "catalogKey": "no candidates",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 0,
                                  "candidateServerKeys": [],
                                  "decisionType": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 0.0,
                                  "decisionReason": "No candidates.",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "validationReason": "No candidates.",
                                  "mappingWritten": false
                                },
                                {
                                  "catalogKey": "no match",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 1,
                                  "candidateServerKeys": [
                                    "possible product"
                                  ],
                                  "decisionType": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 0.34,
                                  "decisionReason": "No semantic match.",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "validationReason": "AI returned NO_MATCH.",
                                  "mappingWritten": false
                                },
                                {
                                  "catalogKey": "low confidence",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 1,
                                  "candidateServerKeys": [
                                    "possible product"
                                  ],
                                  "decisionType": "MATCH",
                                  "selectedServerKey": "possible product",
                                  "confidence": 0.72,
                                  "decisionReason": "Possible but uncertain.",
                                  "validationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "validationReason": "Confidence below 0.80.",
                                  "mappingWritten": false
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val inputs =
                RuntimeKnowledgeGapInputBuilder()
                    .build(
                        catalogFile =
                            catalogFile,
                        serverArtifactFile =
                            nutritionFile,
                        candidateRetrievalFile =
                            requestFile,
                        decisionFile =
                            decisionFile,
                        validationFile =
                            null,
                        catalogServerMappingFile =
                            null,
                        diagnosticsFile =
                            diagnosticsFile
                    )

            assertEquals(
                expected = 3,
                actual = inputs.size
            )

            assertEquals(
                expected =
                    listOf(
                        "low confidence",
                        "no candidates",
                        "no match"
                    ),
                actual =
                    inputs.map {
                        it.catalogKey
                    }
            )

            val noCandidates =
                inputs.single {
                    it.catalogKey ==
                            "no candidates"
                }

            assertTrue(
                noCandidates.candidates.isEmpty()
            )

            assertEquals(
                expected =
                    RuntimeKnowledgeGapDecisionOutcome.NO_MATCH,
                actual =
                    noCandidates.decision?.outcome
            )

            assertEquals(
                expected = 0.0,
                actual =
                    noCandidates.decision?.confidence
            )

            assertFalse(
                noCandidates.validation
                    ?.accepted
                    ?: true
            )

            assertEquals(
                expected =
                    "No candidates.",
                actual =
                    noCandidates.validation?.reason
            )

            assertNull(
                noCandidates.serverEntryExists
            )

            val noMatch =
                inputs.single {
                    it.catalogKey ==
                            "no match"
                }

            assertEquals(
                expected =
                    listOf(
                        "possible product"
                    ),
                actual =
                    noMatch.candidates
            )

            assertEquals(
                expected =
                    RuntimeKnowledgeGapDecisionOutcome.NO_MATCH,
                actual =
                    noMatch.decision?.outcome
            )

            assertNull(
                noMatch.decision
                    ?.selectedServerKey
            )

            assertEquals(
                expected = 0.34,
                actual =
                    noMatch.decision?.confidence
            )

            assertFalse(
                noMatch.validation
                    ?.accepted
                    ?: true
            )

            assertEquals(
                expected =
                    "AI returned NO_MATCH.",
                actual =
                    noMatch.validation?.reason
            )

            assertNull(
                noMatch.serverEntryExists
            )

            val lowConfidence =
                inputs.single {
                    it.catalogKey ==
                            "low confidence"
                }

            assertEquals(
                expected =
                    listOf(
                        "possible product"
                    ),
                actual =
                    lowConfidence.candidates
            )

            assertEquals(
                expected =
                    RuntimeKnowledgeGapDecisionOutcome.MATCH,
                actual =
                    lowConfidence.decision?.outcome
            )

            assertEquals(
                expected =
                    "possible product",
                actual =
                    lowConfidence.decision
                        ?.selectedServerKey
            )

            assertEquals(
                expected = 0.72,
                actual =
                    lowConfidence.decision
                        ?.confidence
            )

            assertEquals(
                expected = 0.80,
                actual =
                    lowConfidence.decision
                        ?.minimumConfidence
            )

            assertFalse(
                lowConfidence.validation
                    ?.accepted
                    ?: true
            )

            assertEquals(
                expected =
                    "Confidence below 0.80.",
                actual =
                    lowConfidence.validation
                        ?.reason
            )

            assertTrue(
                lowConfidence.serverEntryExists
                    ?: false
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}
