package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.knowledge.report.RejectedNutritionRetrievalFailureClassifier
import de.shopme.tools.knowledge.report.RejectedNutritionRetrievalFailureType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RejectedNutritionRetrievalFailureClassifierTest {

    @Test
    fun classifyRejectedNutritionRetrievalFailures() {

        val directory =
            createTempDirectory(
                prefix =
                    "rejected-nutrition-retrieval-failures-"
            )
                .toFile()

        try {
            val inputFile =
                File(
                    directory,
                    "nutrition.rejected-candidate-quality.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "summary": {},
                              "entries": [
                                {
                                  "catalogKey": "no candidate",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 1.0,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "selected lower rank",
                                  "validationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "decisionType": "MATCH",
                                  "decisionConfidence": 0.78,
                                  "selectedServerKey": "second product",
                                  "selectedCandidateRank": 2,
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "first product",
                                      "diagnosticScore": 0.8,
                                      "sharedTokens": [
                                        "product"
                                      ],
                                      "selected": false
                                    },
                                    {
                                      "rank": 2,
                                      "serverKey": "second product",
                                      "diagnosticScore": 0.7,
                                      "sharedTokens": [
                                        "product"
                                      ],
                                      "selected": true
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "unknown source",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "completely different",
                                      "diagnosticScore": 0.6,
                                      "sharedTokens": [],
                                      "selected": false
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "fresh parsley",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.95,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "dried parsley",
                                      "diagnosticScore": 0.7,
                                      "sharedTokens": [
                                        "parsley"
                                      ],
                                      "selected": false
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "soy drink",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.95,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "soy yogurt",
                                      "diagnosticScore": 0.7,
                                      "sharedTokens": [
                                        "soy"
                                      ],
                                      "selected": false
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "organic banana",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "banana",
                                      "diagnosticScore": 0.7,
                                      "sharedTokens": [
                                        "banana"
                                      ],
                                      "selected": false
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "weak product",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "weak candidate",
                                      "diagnosticScore": 0.3,
                                      "sharedTokens": [
                                        "weak"
                                      ],
                                      "selected": false
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "cluster product",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": [
                                    {
                                      "rank": 1,
                                      "serverKey": "cluster first",
                                      "diagnosticScore": 0.70,
                                      "sharedTokens": [
                                        "cluster"
                                      ],
                                      "selected": false
                                    },
                                    {
                                      "rank": 2,
                                      "serverKey": "cluster second",
                                      "diagnosticScore": 0.69,
                                      "sharedTokens": [
                                        "cluster"
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

            val outputFile =
                File(
                    directory,
                    "nutrition.retrieval-failures.json"
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
                        RejectedNutritionRetrievalFailureClassifier()
                            .run(
                                candidateQualityFile =
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
                expected = 8,
                actual =
                    report.summary.rejectedCatalogKeyCount
            )

            assertEquals(
                expected = 8,
                actual =
                    report.summary
                        .primaryTypeCounts
                        .values
                        .sum()
            )

            assertEquals(
                expected =
                    RejectedNutritionRetrievalFailureType.NO_CANDIDATES,
                actual =
                    report.failure(
                        catalogKey = "no candidate"
                    )
                        .primaryType
            )

            assertEquals(
                expected =
                    RejectedNutritionRetrievalFailureType
                        .SELECTED_NOT_TOP_RANKED,
                actual =
                    report.failure(
                        catalogKey = "selected lower rank"
                    )
                        .primaryType
            )

            assertEquals(
                expected =
                    RejectedNutritionRetrievalFailureType
                        .NO_SHARED_TOKENS,
                actual =
                    report.failure(
                        catalogKey = "unknown source"
                    )
                        .primaryType
            )

            assertEquals(
                expected =
                    RejectedNutritionRetrievalFailureType
                        .PROCESSING_STATE_MISMATCH,
                actual =
                    report.failure(
                        catalogKey = "fresh parsley"
                    )
                        .primaryType
            )

            assertEquals(
                expected =
                    RejectedNutritionRetrievalFailureType
                        .PRODUCT_FORM_MISMATCH,
                actual =
                    report.failure(
                        catalogKey = "soy drink"
                    )
                        .primaryType
            )

            assertEquals(
                expected =
                    RejectedNutritionRetrievalFailureType
                        .MODIFIER_MISMATCH,
                actual =
                    report.failure(
                        catalogKey = "organic banana"
                    )
                        .primaryType
            )

            assertEquals(
                expected =
                    RejectedNutritionRetrievalFailureType.VERY_LOW_SCORE,
                actual =
                    report.failure(
                        catalogKey = "weak product"
                    )
                        .primaryType
            )

            assertTrue(
                RejectedNutritionRetrievalFailureType.SCORE_CLUSTER in
                        report.failure(
                            catalogKey = "cluster product"
                        )
                            .signals
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
                        "Complete primary classification : YES"
                    )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun classifyRejectedRetrievalFailuresDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "rejected-retrieval-deterministic-"
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
                              "summary": {},
                              "entries": [
                                {
                                  "catalogKey": "zucchini",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "apple",
                                  "validationStatus": "REJECTED_NO_MATCH",
                                  "decisionType": "NO_MATCH",
                                  "decisionConfidence": 0.9,
                                  "selectedServerKey": null,
                                  "selectedCandidateRank": null,
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

            val classifier =
                RejectedNutritionRetrievalFailureClassifier()

            val first =
                classifier.run(
                    candidateQualityFile =
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
                classifier.run(
                    candidateQualityFile =
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

    private fun de.shopme.tools.knowledge.report
    .RejectedNutritionRetrievalFailureReport.failure(
        catalogKey: String
    ) =
        failures.single {
            it.catalogKey == catalogKey
        }
}