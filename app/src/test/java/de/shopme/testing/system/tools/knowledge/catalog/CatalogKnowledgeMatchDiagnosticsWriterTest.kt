package de.shopme.testing.system.tools.knowledge.mapping.catalog

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDiagnosticsWriter
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogKnowledgeMatchDiagnosticsWriterTest {

    @Test
    fun persistNutritionMatchRejectionDiagnostics() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-match-diagnostics-"
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
                                      "serverKey": "accepted server key"
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "rejected product",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "first candidate"
                                    },
                                    {
                                      "serverKey": "second candidate"
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
                    "nutrition.match-decisions.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "decisions": [
                                {
                                  "catalogKey": "accepted product",
                                  "serverArtifact": "nutrition.json",
                                  "type": "MATCH",
                                  "selectedServerKey": "accepted server key",
                                  "confidence": 0.96,
                                  "reason": "Semantically equivalent."
                                },
                                {
                                  "catalogKey": "rejected product",
                                  "serverArtifact": "nutrition.json",
                                  "type": "NO_MATCH",
                                  "confidence": 0.35,
                                  "reason": "No candidate represents the product."
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val validationFile =
                File(
                    directory,
                    "nutrition.mapping-validation-report.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "validations": [
                                {
                                  "catalogKey": "accepted product",
                                  "serverArtifact": "nutrition.json",
                                  "status": "ACCEPTED",
                                  "reason": "Valid match."
                                },
                                {
                                  "catalogKey": "rejected product",
                                  "serverArtifact": "nutrition.json",
                                  "status": "REJECTED_NO_MATCH",
                                  "reason": "AI returned NO_MATCH."
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
                              "mappings": [
                                {
                                  "catalogKey": "accepted product",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "accepted server key"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "nutrition.match-diagnostics.json"
                )

            val result =
                CatalogKnowledgeMatchDiagnosticsWriter()
                    .write(
                        requestFile =
                            requestFile,
                        decisionFile =
                            decisionFile,
                        validationReportFile =
                            validationFile,
                        mappingFile =
                            mappingFile,
                        outputFile =
                            outputFile
                    )

            assertEquals(
                expected = 2,
                actual =
                    result.diagnostics.size
            )

            val accepted =
                result.diagnostics.single {
                    it.catalogKey ==
                            "accepted product"
                }

            assertEquals(
                expected = 1,
                actual =
                    accepted.candidateCount
            )

            assertEquals(
                expected = "MATCH",
                actual =
                    accepted.decisionType
            )

            assertEquals(
                expected = "ACCEPTED",
                actual =
                    accepted.validationStatus
            )

            assertTrue(
                accepted.mappingWritten
            )

            val rejected =
                result.diagnostics.single {
                    it.catalogKey ==
                            "rejected product"
                }

            assertEquals(
                expected = 2,
                actual =
                    rejected.candidateCount
            )

            assertEquals(
                expected = "NO_MATCH",
                actual =
                    rejected.decisionType
            )

            assertEquals(
                expected = 0.35,
                actual =
                    rejected.confidence
            )

            assertEquals(
                expected =
                    "No candidate represents the product.",
                actual =
                    rejected.decisionReason
            )

            assertEquals(
                expected =
                    "REJECTED_NO_MATCH",
                actual =
                    rejected.validationStatus
            )

            assertFalse(
                rejected.mappingWritten
            )

            assertTrue(
                outputFile.isFile
            )

            val root =
                JsonParser.parseString(
                    outputFile.readText()
                )
                    .asJsonObject

            assertEquals(
                expected = 1,
                actual =
                    root["version"].asInt
            )

            assertEquals(
                expected = 2,
                actual =
                    root["diagnostics"]
                        .asJsonArray
                        .size()
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun writeDiagnosticsDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-match-diagnostics-order-"
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
                                  "serverArtifact": "nutrition.json",
                                  "candidates": []
                                },
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": []
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

            val validationFile =
                File(
                    directory,
                    "validations.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "validations": []
                            }
                            """.trimIndent()
                        )
                    }

            val mappingFile =
                File(
                    directory,
                    "mappings.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "mappings": []
                            }
                            """.trimIndent()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "diagnostics.json"
                )

            val writer =
                CatalogKnowledgeMatchDiagnosticsWriter()

            val first =
                writer.write(
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    validationReportFile =
                        validationFile,
                    mappingFile =
                        mappingFile,
                    outputFile =
                        outputFile
                )

            val firstContent =
                outputFile.readText()

            val second =
                writer.write(
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    validationReportFile =
                        validationFile,
                    mappingFile =
                        mappingFile,
                    outputFile =
                        outputFile
                )

            assertEquals(
                expected = first,
                actual = second
            )

            assertEquals(
                expected = firstContent,
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
                    first.diagnostics.map {
                        it.catalogKey
                    }
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}