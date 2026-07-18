package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.RepresentativeNutritionMappingMerger
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class RepresentativeNutritionMappingMergerTest {

    @Test
    fun mergeAcceptedRepresentativeMappingsDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "representative-nutrition-merge-"
            )
                .toFile()

        try {
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

            val validationFile =
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
                                  "originalValidationStatus":
                                    "REJECTED_LOW_CONFIDENCE",
                                  "decisionType": "REPRESENTATIVE",
                                  "reasons": [
                                    "SAME_PRODUCT_CLASS"
                                  ],
                                  "accepted": true
                                },
                                {
                                  "catalogKey": "fish sausage",
                                  "selectedServerKey": "pork sausage",
                                  "candidateRank": 1,
                                  "originalConfidence": 0.78,
                                  "originalValidationStatus":
                                    "REJECTED_LOW_CONFIDENCE",
                                  "decisionType": "INCOMPATIBLE",
                                  "reasons": [
                                    "PRODUCT_CLASS_MISMATCH"
                                  ],
                                  "accepted": false
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val merger =
                RepresentativeNutritionMappingMerger()

            val first =
                merger.merge(
                    representativeValidationFile =
                        validationFile,
                    mappingFile =
                        mappingFile
                )

            val firstContent =
                mappingFile.readText()

            val second =
                merger.merge(
                    representativeValidationFile =
                        validationFile,
                    mappingFile =
                        mappingFile
                )

            assertEquals(
                expected = 1,
                actual =
                    first.existingMappingCount
            )

            assertEquals(
                expected = 1,
                actual =
                    first.representativeMappingCount
            )

            assertEquals(
                expected = 1,
                actual =
                    first.representativeAddedCount
            )

            assertEquals(
                expected = 0,
                actual =
                    first.representativeUnchangedCount
            )

            assertEquals(
                expected = 2,
                actual =
                    first.finalMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    second.representativeAddedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    second.representativeUnchangedCount
            )

            assertEquals(
                expected =
                    firstContent,
                actual =
                    mappingFile.readText()
            )

            val mappings =
                JsonParser.parseString(
                    mappingFile.readText()
                )
                    .asJsonObject["mappings"]
                    .asJsonArray

            assertEquals(
                expected = 2,
                actual =
                    mappings.size()
            )

            assertTrue(
                mappings.any {
                    it.asJsonObject["catalogKey"].asString ==
                            "fruit yogurt"
                }
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun rejectConflictingRepresentativeMapping() {

        val directory =
            createTempDirectory(
                prefix =
                    "representative-nutrition-conflict-"
            )
                .toFile()

        try {
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
                                  "catalogKey": "fruit yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "plain yogurt"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val validationFile =
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
                                  "decisionType": "REPRESENTATIVE",
                                  "accepted": true
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val exception =
                assertFailsWith<IllegalStateException> {

                    RepresentativeNutritionMappingMerger()
                        .merge(
                            representativeValidationFile =
                                validationFile,
                            mappingFile =
                                mappingFile
                        )
                }

            assertTrue(
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
}