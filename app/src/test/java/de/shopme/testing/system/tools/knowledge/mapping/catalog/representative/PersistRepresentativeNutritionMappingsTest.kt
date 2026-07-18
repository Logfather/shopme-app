package de.shopme.testing.system.tools.knowledge.mapping.catalog.representative

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.representative.PersistRepresentativeNutritionMappings
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PersistRepresentativeNutritionMappingsTest {

    @Test
    fun persistAcceptedRepresentativeNutritionMappings() {

        val directory =
            createTempDirectory(
                prefix =
                    "persist-representative-nutrition-"
            )
                .toFile()

        try {
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
                              "summary": {
                                "rejectedLowConfidenceCount": 3,
                                "identicalCount": 1,
                                "representativeCount": 1,
                                "incompatibleCount": 1,
                                "acceptedCount": 2,
                                "stillRejectedCount": 1
                              },
                              "entries": [
                                {
                                  "catalogKey": "apple",
                                  "selectedServerKey": "apple raw",
                                  "candidateRank": 1,
                                  "originalConfidence": 0.75,
                                  "originalDecisionReason": "Possible match.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Below threshold.",
                                  "decisionType": "IDENTICAL",
                                  "reasons": [
                                    "SAME_PRODUCT_CLASS"
                                  ],
                                  "accepted": true
                                },
                                {
                                  "catalogKey": "cherry fruit yogurt",
                                  "selectedServerKey": "lowfat black cherry yogurt",
                                  "candidateRank": 2,
                                  "originalConfidence": 0.72,
                                  "originalDecisionReason": "Possible match.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Below threshold.",
                                  "decisionType": "REPRESENTATIVE",
                                  "reasons": [
                                    "COMPATIBLE_SPECIALIZATION"
                                  ],
                                  "accepted": true
                                },
                                {
                                  "catalogKey": "fish sausage",
                                  "selectedServerKey": "pork sausage",
                                  "candidateRank": 1,
                                  "originalConfidence": 0.70,
                                  "originalDecisionReason": "Possible match.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Below threshold.",
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
                                  "catalogKey": "banana",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "banana raw"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val outputBytes =
                ByteArrayOutputStream()

            val result =
                PrintStream(
                    outputBytes,
                    true,
                    Charsets.UTF_8.name()
                )
                    .use { output ->

                        PersistRepresentativeNutritionMappings()
                            .run(
                                validationFile =
                                    validationFile,
                                mappingFile =
                                    mappingFile,
                                output =
                                    output
                            )
                    }

            assertEquals(
                expected = 3,
                actual =
                    result.validationEntryCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.acceptedValidationCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.existingMappingCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.addedMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.unchangedMappingCount
            )

            assertEquals(
                expected = 3,
                actual =
                    result.finalMappingCount
            )

            val root =
                JsonParser.parseString(
                    mappingFile.readText()
                )
                    .asJsonObject

            assertEquals(
                expected = 1,
                actual =
                    root["version"]
                        .asInt
            )

            val mappings =
                root["mappings"]
                    .asJsonArray

            assertEquals(
                expected = 3,
                actual =
                    mappings.size()
            )

            assertEquals(
                expected =
                    listOf(
                        "apple",
                        "banana",
                        "cherry fruit yogurt"
                    ),
                actual =
                    mappings.map {
                        it.asJsonObject["catalogKey"]
                            .asString
                    }
            )

            assertTrue(
                mappings.none {
                    it.asJsonObject["catalogKey"]
                        .asString ==
                            "fish sausage"
                }
            )

            val consoleOutput =
                outputBytes.toString(
                    Charsets.UTF_8.name()
                )

            assertTrue(
                consoleOutput.contains(
                    "PERSIST REPRESENTATIVE NUTRITION MAPPINGS"
                )
            )

            assertTrue(
                consoleOutput.contains(
                    "Added mappings           : 2"
                )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun preserveExistingEquivalentMappingDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "persist-equivalent-representative-"
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
                            """
                            {
                              "version": 1,
                              "entries": [
                                {
                                  "catalogKey": "banana",
                                  "selectedServerKey": "banana raw",
                                  "decisionType": "REPRESENTATIVE",
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
                    "mappings.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "mappings": [
                                {
                                  "catalogKey": "banana",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "banana raw"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val persister =
                PersistRepresentativeNutritionMappings()

            val first =
                persister.run(
                    validationFile =
                        validationFile,
                    mappingFile =
                        mappingFile,
                    output =
                        PrintStream(
                            ByteArrayOutputStream()
                        )
                )

            val firstContent =
                mappingFile.readText()

            val second =
                persister.run(
                    validationFile =
                        validationFile,
                    mappingFile =
                        mappingFile,
                    output =
                        PrintStream(
                            ByteArrayOutputStream()
                        )
                )

            assertEquals(
                expected = 0,
                actual =
                    first.addedMappingCount
            )

            assertEquals(
                expected = 1,
                actual =
                    first.unchangedMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    second.addedMappingCount
            )

            assertEquals(
                expected = 1,
                actual =
                    second.unchangedMappingCount
            )

            assertEquals(
                expected =
                    firstContent,
                actual =
                    mappingFile.readText()
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun rejectConflictingExistingMapping() {

        val directory =
            createTempDirectory(
                prefix =
                    "persist-conflicting-representative-"
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
                            """
                            {
                              "version": 1,
                              "entries": [
                                {
                                  "catalogKey": "banana",
                                  "selectedServerKey": "banana cooked",
                                  "decisionType": "REPRESENTATIVE",
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
                    "mappings.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "mappings": [
                                {
                                  "catalogKey": "banana",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "banana raw"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val originalContent =
                mappingFile.readText()

            val exception =
                assertFailsWith<IllegalStateException> {

                    PersistRepresentativeNutritionMappings()
                        .run(
                            validationFile =
                                validationFile,
                            mappingFile =
                                mappingFile,
                            output =
                                PrintStream(
                                    ByteArrayOutputStream()
                                )
                        )
                }

            assertTrue(
                exception.message
                    .orEmpty()
                    .contains(
                        "Conflicting representative nutrition mapping"
                    )
            )

            assertEquals(
                expected =
                    originalContent,
                actual =
                    mappingFile.readText()
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}