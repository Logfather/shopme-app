package de.shopme.testing.system.tools.knowledge.mapping.catalog.training

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType
import de.shopme.tools.knowledge.mapping.catalog.training.RepresentativeNutritionMappingTrainingExampleExporter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentativeNutritionMappingTrainingExampleExporterTest {

    @Test
    fun exportAcceptedRepresentativeNutritionTrainingExamples() {

        val directory =
            createTempDirectory(
                prefix =
                    "representative-nutrition-training-"
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
                                  "catalogKey": "Cherry Fruit Yogurt",
                                  "selectedServerKey": "Lowfat Black Cherry Yogurt",
                                  "candidateRank": 2,
                                  "originalConfidence": 0.72,
                                  "originalDecisionReason": "Possible semantic match.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Confidence below threshold.",
                                  "decisionType": "REPRESENTATIVE",
                                  "reasons": [
                                    "SHARED_CORE_TOKEN",
                                    "COMPATIBLE_SPECIALIZATION",
                                    "SHARED_CORE_TOKEN"
                                  ],
                                  "accepted": true
                                },
                                {
                                  "catalogKey": "Apple",
                                  "selectedServerKey": "Apple Raw",
                                  "candidateRank": 1,
                                  "originalConfidence": 0.79,
                                  "originalDecisionReason": "Same product.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Confidence below threshold.",
                                  "decisionType": "IDENTICAL",
                                  "reasons": [
                                    "SAME_PRODUCT_CLASS"
                                  ],
                                  "accepted": true
                                },
                                {
                                  "catalogKey": "Fish Sausage",
                                  "selectedServerKey": "Pork Sausage",
                                  "candidateRank": 1,
                                  "originalConfidence": 0.70,
                                  "originalDecisionReason": "Possible match.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Confidence below threshold.",
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

            val outputFile =
                File(
                    directory,
                    "training-examples.json"
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

                        RepresentativeNutritionMappingTrainingExampleExporter()
                            .run(
                                validationFile =
                                    validationFile,
                                outputFile =
                                    outputFile,
                                output =
                                    output
                            )
                    }

            val dataset =
                result.dataset

            assertEquals(
                expected = 3,
                actual =
                    dataset.summary.sourceEntryCount
            )

            assertEquals(
                expected = 2,
                actual =
                    dataset.summary.exportedExampleCount
            )

            assertEquals(
                expected = 1,
                actual =
                    dataset.summary.identicalCount
            )

            assertEquals(
                expected = 1,
                actual =
                    dataset.summary.representativeCount
            )

            assertEquals(
                expected =
                    listOf(
                        "apple",
                        "cherry fruit yogurt"
                    ),
                actual =
                    dataset.examples.map {
                        it.catalogKey
                    }
            )

            val apple =
                dataset.examples.first()

            assertEquals(
                expected =
                    RepresentativeNutritionMappingDecisionType.IDENTICAL,
                actual =
                    apple.decisionType
            )

            assertEquals(
                expected = "nutrition.json",
                actual =
                    apple.serverArtifact
            )

            assertEquals(
                expected = 64,
                actual =
                    apple.id.length
            )

            val yogurt =
                dataset.examples.last()

            assertEquals(
                expected =
                    listOf(
                        "COMPATIBLE_SPECIALIZATION",
                        "SHARED_CORE_TOKEN"
                    ),
                actual =
                    yogurt.reasons
            )

            assertTrue(
                yogurt.accepted
            )

            assertTrue(
                outputFile.isFile
            )

            val persisted =
                JsonParser.parseString(
                    outputFile.readText()
                )
                    .asJsonObject

            assertEquals(
                expected = 1,
                actual =
                    persisted["version"]
                        .asInt
            )

            assertEquals(
                expected =
                    "REPRESENTATIVE_NUTRITION_MAPPING",
                actual =
                    persisted["datasetType"]
                        .asString
            )

            val persistedExamples =
                persisted["examples"]
                    .asJsonArray

            assertEquals(
                expected = 2,
                actual =
                    persistedExamples.size()
            )

            assertFalse(
                persistedExamples.any {
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
                    "REPRESENTATIVE NUTRITION TRAINING EXAMPLES"
                )
            )

            assertTrue(
                consoleOutput.contains(
                    "Exported examples        : 2"
                )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun writeTrainingExamplesDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "representative-training-order-"
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
                                  "catalogKey": "Zucchini",
                                  "selectedServerKey": "Zucchini Raw",
                                  "candidateRank": 1,
                                  "originalConfidence": 0.76,
                                  "originalDecisionReason": "Possible match.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Below threshold.",
                                  "decisionType": "REPRESENTATIVE",
                                  "reasons": [
                                    "SAME_PRODUCT_CLASS"
                                  ],
                                  "accepted": true
                                },
                                {
                                  "catalogKey": "Apple",
                                  "selectedServerKey": "Apple Raw",
                                  "candidateRank": 1,
                                  "originalConfidence": 0.78,
                                  "originalDecisionReason": "Possible match.",
                                  "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                                  "originalValidationReason": "Below threshold.",
                                  "decisionType": "IDENTICAL",
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

            val outputFile =
                File(
                    directory,
                    "training.json"
                )

            val exporter =
                RepresentativeNutritionMappingTrainingExampleExporter()

            val first =
                exporter.run(
                    validationFile =
                        validationFile,
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
                    validationFile =
                        validationFile,
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
                expected =
                    listOf(
                        "apple",
                        "zucchini"
                    ),
                actual =
                    first.dataset.examples.map {
                        it.catalogKey
                    }
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}