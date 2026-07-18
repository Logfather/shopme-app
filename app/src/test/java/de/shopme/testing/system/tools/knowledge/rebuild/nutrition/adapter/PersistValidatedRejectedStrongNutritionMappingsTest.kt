package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.rebuild.nutrition.adapter.PersistValidatedRejectedStrongNutritionMappings
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PersistValidatedRejectedStrongNutritionMappingsTest {

    @Test
    fun addAcceptedMappingsAndRemainIdempotent() {

        val directory =
            Files.createTempDirectory(
                "persist-rejected-strong-nutrition"
            )
                .toFile()

        try {
            val validationFile =
                File(
                    directory,
                    "validation.json"
                )
                    .apply {
                        writeText(
                            validationJson()
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

            val persister =
                PersistValidatedRejectedStrongNutritionMappings(
                    printLine =
                        {}
                )

            val first =
                persister.run(
                    validationFile =
                        validationFile,
                    mappingFile =
                        mappingFile
                )

            assertEquals(
                expected =
                    1,
                actual =
                    first.existingMappingCount
            )

            assertEquals(
                expected =
                    3,
                actual =
                    first.addedMappingCount
            )

            assertEquals(
                expected =
                    0,
                actual =
                    first.unchangedMappingCount
            )

            assertEquals(
                expected =
                    4,
                actual =
                    first.finalMappingCount
            )

            val contentAfterFirstRun =
                mappingFile.readText()

            val second =
                persister.run(
                    validationFile =
                        validationFile,
                    mappingFile =
                        mappingFile
                )

            assertEquals(
                expected =
                    4,
                actual =
                    second.existingMappingCount
            )

            assertEquals(
                expected =
                    0,
                actual =
                    second.addedMappingCount
            )

            assertEquals(
                expected =
                    3,
                actual =
                    second.unchangedMappingCount
            )

            assertEquals(
                expected =
                    4,
                actual =
                    second.finalMappingCount
            )

            assertEquals(
                expected =
                    contentAfterFirstRun,
                actual =
                    mappingFile.readText(),
                message =
                    "Idempotent persistence must not rewrite an " +
                            "unchanged mapping file."
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun rejectConflictWithExistingMapping() {

        val directory =
            Files.createTempDirectory(
                "persist-rejected-strong-conflict"
            )
                .toFile()

        try {
            val validationFile =
                File(
                    directory,
                    "validation.json"
                )
                    .apply {
                        writeText(
                            validationJson()
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
                                  "catalogKey": "organic egg noodles",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "plain wheat noodles"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val exception =
                assertFailsWith<IllegalStateException> {

                    PersistValidatedRejectedStrongNutritionMappings(
                        printLine =
                            {}
                    )
                        .run(
                            validationFile =
                                validationFile,
                            mappingFile =
                                mappingFile
                        )
                }

            assertTrue(
                actual =
                    exception.message
                        .orEmpty()
                        .contains(
                            "conflicts with an existing mapping"
                        )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    private fun validationJson():
            String =
        """
        {
          "version": 1,
          "candidateCount": 3,
          "acceptedCount": 3,
          "rejectedCount": 0,
          "entries": [
            {
              "catalogKey": "fresh whole grain pasta",
              "selectedServerKey": "whole grain pasta",
              "diagnosticType": "COMPATIBLE_GENERALIZATION",
              "originalNoMatchCause": "STRONG_TOP_CANDIDATE_REJECTED",
              "originalConfidence": 0.9,
              "candidateRank": 1,
              "diagnosticScore": 0.9,
              "sharedTokens": [
                "grain",
                "pasta",
                "whole"
              ],
              "decisionType": "REPRESENTATIVE",
              "accepted": true,
              "reasons": [
                "SAME_PRODUCT_CLASS",
                "COMPATIBLE_SPECIALIZATION",
                "COMPATIBLE_PREPARATION"
              ],
              "details": "Accepted."
            },
            {
              "catalogKey": "organic canned white beans",
              "selectedServerKey": "organic white beans",
              "diagnosticType": "COMPATIBLE_GENERALIZATION",
              "originalNoMatchCause": "STRONG_TOP_CANDIDATE_REJECTED",
              "originalConfidence": 0.9,
              "candidateRank": 1,
              "diagnosticScore": 0.9,
              "sharedTokens": [
                "beans",
                "organic",
                "white"
              ],
              "decisionType": "REPRESENTATIVE",
              "accepted": true,
              "reasons": [
                "SAME_PRODUCT_CLASS",
                "COMPATIBLE_SPECIALIZATION"
              ],
              "details": "Accepted."
            },
            {
              "catalogKey": "organic egg noodles",
              "selectedServerKey": "organic wide egg noodles",
              "diagnosticType": "ADDITIONAL_NON_CRITICAL_MODIFIER",
              "originalNoMatchCause": "STRONG_TOP_CANDIDATE_REJECTED",
              "originalConfidence": 0.9,
              "candidateRank": 1,
              "diagnosticScore": 0.9,
              "sharedTokens": [
                "egg",
                "noodles",
                "organic"
              ],
              "decisionType": "REPRESENTATIVE",
              "accepted": true,
              "reasons": [
                "SAME_PRODUCT_CLASS",
                "COMPATIBLE_SPECIALIZATION",
                "COMPATIBLE_VARIANT"
              ],
              "details": "Accepted."
            }
          ]
        }
        """.trimIndent()
}