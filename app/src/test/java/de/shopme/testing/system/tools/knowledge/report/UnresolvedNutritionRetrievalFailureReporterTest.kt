package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.knowledge.report.UnresolvedNutritionCatalogKeyLengthBand
import de.shopme.tools.knowledge.report.UnresolvedNutritionRetrievalFailureReporter
import de.shopme.tools.knowledge.report.UnresolvedNutritionScoreBand
import de.shopme.tools.knowledge.report.UnresolvedNutritionScoreDeltaBand
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnresolvedNutritionRetrievalFailureReporterTest {

    @Test
    fun reportUnresolvedNutritionRetrievalFailures() {

        val directory =
            createTempDirectory(
                prefix =
                    "unresolved-nutrition-retrieval-"
            )
                .toFile()

        try {
            val inputFile =
                File(
                    directory,
                    "nutrition.retrieval-failures.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "thresholds": {},
                              "summary": {},
                              "failures": [
                                {
                                  "catalogKey": "ignored known failure",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.95,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "primaryType": "SCORE_CLUSTER",
                                  "signals": [
                                    "SCORE_CLUSTER"
                                  ],
                                  "metrics": {
                                    "candidateCount": 2,
                                    "topCandidateScore": 0.70,
                                    "secondCandidateScore": 0.69,
                                    "topScoreDelta": 0.01,
                                    "maximumSharedTokenCount": 1,
                                    "catalogTokenCount": 3,
                                    "topCandidateTokenCount": 3,
                                    "topCandidateTokenRatio": 1.0
                                  },
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "high score",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.96,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "primaryType": "UNKNOWN",
                                  "signals": [
                                    "UNKNOWN"
                                  ],
                                  "metrics": {
                                    "candidateCount": 2,
                                    "topCandidateScore": 0.82,
                                    "secondCandidateScore": 0.65,
                                    "topScoreDelta": 0.17,
                                    "maximumSharedTokenCount": 2,
                                    "catalogTokenCount": 2,
                                    "topCandidateTokenCount": 2,
                                    "topCandidateTokenRatio": 1.0
                                  },
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "high score candidate",
                                      "diagnosticScore": 0.82,
                                      "sharedTokens": [
                                        "high",
                                        "score"
                                      ],
                                      "selected": false
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "medium score product",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.85,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "primaryType": "UNKNOWN",
                                  "signals": [
                                    "UNKNOWN"
                                  ],
                                  "metrics": {
                                    "candidateCount": 2,
                                    "topCandidateScore": 0.65,
                                    "secondCandidateScore": 0.61,
                                    "topScoreDelta": 0.04,
                                    "maximumSharedTokenCount": 2,
                                    "catalogTokenCount": 3,
                                    "topCandidateTokenCount": 3,
                                    "topCandidateTokenRatio": 1.0
                                  },
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "very long unresolved catalog product key",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.70,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "primaryType": "UNKNOWN",
                                  "signals": [
                                    "UNKNOWN"
                                  ],
                                  "metrics": {
                                    "candidateCount": 1,
                                    "topCandidateScore": 0.50,
                                    "secondCandidateScore": null,
                                    "topScoreDelta": null,
                                    "maximumSharedTokenCount": 2,
                                    "catalogTokenCount": 6,
                                    "topCandidateTokenCount": 3,
                                    "topCandidateTokenRatio": 0.5
                                  },
                                  "candidates": []
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "nutrition.unresolved-retrieval-failures.json"
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
                        UnresolvedNutritionRetrievalFailureReporter()
                            .run(
                                retrievalFailureFile =
                                    inputFile,
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
                    report.summary.unresolvedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary.highTopScoreCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary.mediumTopScoreCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary.lowTopScoreCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary.largeScoreDeltaCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary.mediumScoreDeltaCount
            )

            assertEquals(
                expected = 1,
                actual =
                    report.summary.missingScoreDeltaCount
            )

            val highScore =
                report.failures.single {
                    it.catalogKey ==
                            "high score"
                }

            assertEquals(
                expected =
                    UnresolvedNutritionScoreBand.HIGH,
                actual =
                    highScore.scoreBand
            )

            assertEquals(
                expected =
                    UnresolvedNutritionScoreDeltaBand.LARGE,
                actual =
                    highScore.scoreDeltaBand
            )

            assertEquals(
                expected =
                    UnresolvedNutritionCatalogKeyLengthBand.SHORT,
                actual =
                    highScore.catalogKeyLengthBand
            )

            val longKey =
                report.failures.single {
                    it.catalogKey ==
                            "very long unresolved catalog product key"
                }

            assertEquals(
                expected =
                    UnresolvedNutritionScoreBand.LOW,
                actual =
                    longKey.scoreBand
            )

            assertEquals(
                expected =
                    UnresolvedNutritionScoreDeltaBand.MISSING,
                actual =
                    longKey.scoreDeltaBand
            )

            assertEquals(
                expected =
                    UnresolvedNutritionCatalogKeyLengthBand.LONG,
                actual =
                    longKey.catalogKeyLengthBand
            )

            assertTrue(
                outputFile.isFile
            )

            assertTrue(
                outputBytes
                    .toString(
                        Charsets.UTF_8.name()
                    )
                    .contains(
                        "UNRESOLVED NUTRITION RETRIEVAL FAILURES"
                    )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun writeUnresolvedRetrievalReportDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "unresolved-nutrition-deterministic-"
            )
                .toFile()

        try {
            val inputFile =
                File(
                    directory,
                    "input.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "failures": [
                                {
                                  "catalogKey": "zucchini",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "primaryType": "UNKNOWN",
                                  "metrics": {
                                    "candidateCount": 1,
                                    "topCandidateScore": 0.6,
                                    "secondCandidateScore": null,
                                    "topScoreDelta": null,
                                    "maximumSharedTokenCount": 1,
                                    "catalogTokenCount": 1,
                                    "topCandidateTokenCount": 1,
                                    "topCandidateTokenRatio": 1.0
                                  },
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "apple",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "primaryType": "UNKNOWN",
                                  "metrics": {
                                    "candidateCount": 1,
                                    "topCandidateScore": 0.6,
                                    "secondCandidateScore": null,
                                    "topScoreDelta": null,
                                    "maximumSharedTokenCount": 1,
                                    "catalogTokenCount": 1,
                                    "topCandidateTokenCount": 1,
                                    "topCandidateTokenRatio": 1.0
                                  },
                                  "candidates": []
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "output.json"
                )

            val reporter =
                UnresolvedNutritionRetrievalFailureReporter()

            val first =
                reporter.run(
                    retrievalFailureFile =
                        inputFile,
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
                    retrievalFailureFile =
                        inputFile,
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
                    first.report.failures.map {
                        it.catalogKey
                    }
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}