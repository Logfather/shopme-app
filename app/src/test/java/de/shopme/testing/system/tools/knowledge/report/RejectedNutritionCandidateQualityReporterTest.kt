package de.shopme.testing.system.tools.knowledge.report

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.report.RejectedNutritionCandidateQualityReporter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RejectedNutritionCandidateQualityReporterTest {

    @Test
    fun reportRejectedNutritionCandidateQuality() {

        val directory =
            createTempDirectory(
                prefix =
                    "rejected-nutrition-candidate-quality-"
            )
                .toFile()

        try {
            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "requests": [
                                {
                                  "catalogKey": "accepted product",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "accepted candidate",
                                      "diagnosticScore": 10.0,
                                      "sharedTokens": [
                                        "accepted"
                                      ]
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "no candidate product",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "no match product",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "first wrong candidate",
                                      "diagnosticScore": 8.0,
                                      "sharedTokens": [
                                        "product"
                                      ]
                                    },
                                    {
                                      "serverKey": "second wrong candidate",
                                      "diagnosticScore": 5.0,
                                      "sharedTokens": []
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "low confidence product",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "first candidate",
                                      "diagnosticScore": 9.0,
                                      "sharedTokens": [
                                        "product"
                                      ]
                                    },
                                    {
                                      "serverKey": "selected candidate",
                                      "diagnosticScore": 7.0,
                                      "sharedTokens": [
                                        "low",
                                        "product"
                                      ]
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
                                  "catalogKey": "accepted product",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 1,
                                  "candidateServerKeys": [
                                    "accepted candidate"
                                  ],
                                  "decisionType": "MATCH",
                                  "selectedServerKey": "accepted candidate",
                                  "confidence": 0.96,
                                  "decisionReason": "Valid match.",
                                  "validationStatus": "ACCEPTED",
                                  "validationReason": "Accepted.",
                                  "mappingWritten": true
                                },
                                {
                                  "catalogKey": "no candidate product",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 0,
                                  "candidateServerKeys": [],
                                  "decisionType": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 1.0,
                                  "decisionReason": "No candidates.",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "validationReason": "No candidates.",
                                  "mappingWritten": false
                                },
                                {
                                  "catalogKey": "no match product",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 2,
                                  "candidateServerKeys": [
                                    "first wrong candidate",
                                    "second wrong candidate"
                                  ],
                                  "decisionType": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 0.92,
                                  "decisionReason": "Candidates are unrelated.",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "validationReason": "AI returned NO_MATCH.",
                                  "mappingWritten": false
                                },
                                {
                                  "catalogKey": "low confidence product",
                                  "serverArtifact": "nutrition.json",
                                  "candidateCount": 2,
                                  "candidateServerKeys": [
                                    "first candidate",
                                    "selected candidate"
                                  ],
                                  "decisionType": "MATCH",
                                  "selectedServerKey": "selected candidate",
                                  "confidence": 0.78,
                                  "decisionReason": "Possible semantic match.",
                                  "validationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "validationReason": "Confidence below threshold.",
                                  "mappingWritten": false
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "nutrition.rejected-candidate-quality.json"
                )

            val outputBytes =
                ByteArrayOutputStream()

            val result =
                PrintStream(
                    outputBytes,
                    true,
                    Charsets.UTF_8.name()
                )
                    .use { output ->
                        RejectedNutritionCandidateQualityReporter()
                            .run(
                                requestFile =
                                    requestFile,
                                diagnosticsFile =
                                    diagnosticsFile,
                                outputFile =
                                    outputFile,
                                output =
                                    output
                            )
                    }

            val report =
                result.report

            assertEquals(
                expected = 3,
                actual =
                    report.summary
                        .rejectedCatalogKeyCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary
                        .noCandidateCount
            )

            assertEquals(
                expected = 2,
                actual =
                    report.summary
                        .noMatchCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary
                        .lowConfidenceCount
            )

            assertEquals(
                expected = 4,
                actual =
                    report.summary
                        .candidateCount
            )

            assertEquals(
                expected =
                    mapOf(
                        0 to 1,
                        2 to 2
                    ),
                actual =
                    report.summary
                        .candidateCountDistribution
            )

            assertEquals(
                expected =
                    mapOf(
                        2 to 1
                    ),
                actual =
                    report.summary
                        .selectedLowConfidenceRankCounts
            )

            assertEquals(
                expected =
                    listOf(
                        "low confidence product",
                        "no candidate product",
                        "no match product"
                    ),
                actual =
                    report.entries.map {
                        it.catalogKey
                    }
            )

            val lowConfidence =
                report.entries.single {
                    it.catalogKey ==
                            "low confidence product"
                }

            assertEquals(
                expected = 2,
                actual =
                    lowConfidence.selectedCandidateRank
            )

            assertFalse(
                lowConfidence.candidates
                    .first()
                    .selected
            )

            assertTrue(
                lowConfidence.candidates
                    .last()
                    .selected
            )

            assertEquals(
                expected =
                    listOf(
                        "low",
                        "product"
                    ),
                actual =
                    lowConfidence.candidates
                        .last()
                        .sharedTokens
            )

            assertTrue(
                outputFile.isFile
            )

            val persistedRoot =
                JsonParser.parseString(
                    outputFile.readText()
                )
                    .asJsonObject

            assertEquals(
                expected = 1,
                actual =
                    persistedRoot["version"]
                        .asInt
            )

            assertEquals(
                expected = 3,
                actual =
                    persistedRoot["entries"]
                        .asJsonArray
                        .size()
            )

            val consoleOutput =
                outputBytes.toString(
                    Charsets.UTF_8.name()
                )

            assertTrue(
                consoleOutput.contains(
                    "REJECTED NUTRITION CANDIDATE QUALITY"
                )
            )

            assertTrue(
                consoleOutput.contains(
                    "Rejected catalog keys      : 3"
                )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun writeRejectedCandidateQualityDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "rejected-candidate-quality-order-"
            )
                .toFile()

        try {
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
                                  "catalogKey": "zucchini",
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "apple",
                                  "candidates": []
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val diagnosticsFile =
                File(
                    directory,
                    "diagnostics.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "diagnostics": [
                                {
                                  "catalogKey": "zucchini",
                                  "decisionType": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 0.9,
                                  "decisionReason": "No match.",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "validationReason": "Rejected.",
                                  "mappingWritten": false
                                },
                                {
                                  "catalogKey": "apple",
                                  "decisionType": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 0.9,
                                  "decisionReason": "No match.",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "validationReason": "Rejected.",
                                  "mappingWritten": false
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "report.json"
                )

            val reporter =
                RejectedNutritionCandidateQualityReporter()

            val first =
                reporter.run(
                    requestFile =
                        requestFile,
                    diagnosticsFile =
                        diagnosticsFile,
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
                reporter.run(
                    requestFile =
                        requestFile,
                    diagnosticsFile =
                        diagnosticsFile,
                    outputFile =
                        outputFile,
                    output =
                        PrintStream(
                            ByteArrayOutputStream()
                        )
                )

            assertEquals(
                expected =
                    first.report,
                actual =
                    second.report
            )

            assertEquals(
                expected =
                    firstContent,
                actual =
                    outputFile.readText()
            )

            assertEquals(
                expected =
                    listOf(
                        "apple",
                        "zucchini"
                    ),
                actual =
                    first.report.entries.map {
                        it.catalogKey
                    }
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}