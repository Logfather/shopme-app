package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.runner.WriteValidatedNutritionCatalogServerMappingsResult
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.DefaultNutritionKnowledgeMappingPersistenceStep
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNutritionKnowledgeMappingPersistenceStepTest {

    @Test
    fun reportAddedAndUnchangedMappings() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-mapping-persistence-"
            )
                .toFile()

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
                  "entries": []
                }
                """.trimIndent()
                    )
                }

        try {
            val outputMappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )
                    .apply {
                        writeMappings(
                            entries =
                                listOf(
                                    MappingFixture(
                                        catalogKey =
                                            "apple",
                                        serverKey =
                                            "apple raw"
                                    )
                                )
                        )
                    }

            val validationReportFile =
                File(
                    directory,
                    "nutrition.mapping-validation-report.json"
                )

            val result =
                DefaultNutritionKnowledgeMappingPersistenceStep(

                    representativeValidationFile =
                        representativeValidationFile,
                    outputMappingFile =
                        outputMappingFile,
                    persistMappings = {

                        outputMappingFile.writeMappings(
                            entries =
                                listOf(
                                    MappingFixture(
                                        catalogKey =
                                            "apple",
                                        serverKey =
                                            "apple raw"
                                    ),
                                    MappingFixture(
                                        catalogKey =
                                            "banana",
                                        serverKey =
                                            "banana raw"
                                    ),
                                    MappingFixture(
                                        catalogKey =
                                            "cherry yogurt",
                                        serverKey =
                                            "cherry fruit yogurt"
                                    )
                                )
                        )

                        validationReportFile.writeText(
                            """
                            {
                              "version": 1,
                              "validations": []
                            }
                            """.trimIndent()
                        )

                        WriteValidatedNutritionCatalogServerMappingsResult(
                            requestCount =
                                3,
                            decisionCount =
                                3,
                            diagnosticCount =
                                0,
                            serverKeyCount =
                                100,
                            exactMappingCount =
                                0,
                            acceptedMappingCount =
                                3,
                            rejectedDecisionCount =
                                0,
                            validationStatusCounts =
                                mapOf(
                                    "ACCEPTED" to 3
                                ),
                            validationReportFile =
                                validationReportFile.path,
                            outputMappingFile =
                                outputMappingFile.path,
                            diagnosticsFile =
                                File(
                                    directory,
                                    "nutrition.match-diagnostics.json"
                                ).path
                        )
                    }
                )
                    .run()

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
                expected = 1,
                actual =
                    result.unchangedMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.conflictCount
            )

            assertEquals(
                expected = 3,
                actual =
                    result.finalMappingCount
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun reportIdempotentPersistenceWithoutAddedMappings() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-mapping-idempotent-"
            )
                .toFile()

        try {
            val outputMappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )
                    .apply {
                        writeMappings(
                            entries =
                                listOf(
                                    MappingFixture(
                                        catalogKey =
                                            "apple",
                                        serverKey =
                                            "apple raw"
                                    ),
                                    MappingFixture(
                                        catalogKey =
                                            "banana",
                                        serverKey =
                                            "banana raw"
                                    )
                                )
                        )
                    }

            val validationReportFile =
                File(
                    directory,
                    "nutrition.mapping-validation-report.json"
                )

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
                  "entries": []
                }
                """.trimIndent()
                        )
                    }

            val originalContent =
                outputMappingFile.readText()

            val result =
                DefaultNutritionKnowledgeMappingPersistenceStep(

                    representativeValidationFile =
                        representativeValidationFile,
                    outputMappingFile =
                        outputMappingFile,
                    persistMappings = {

                        validationReportFile.writeText(
                            """
                            {
                              "version": 1,
                              "validations": []
                            }
                            """.trimIndent()
                        )

                        WriteValidatedNutritionCatalogServerMappingsResult(
                            requestCount =
                                2,
                            decisionCount =
                                2,
                            diagnosticCount =
                                0,
                            serverKeyCount =
                                100,
                            exactMappingCount =
                                0,
                            acceptedMappingCount =
                                2,
                            rejectedDecisionCount =
                                0,
                            validationStatusCounts =
                                mapOf(
                                    "ACCEPTED" to 2
                                ),
                            outputMappingFile =
                                outputMappingFile.path,
                            validationReportFile =
                                validationReportFile.path,
                            diagnosticsFile =
                                File(
                                    directory,
                                    "nutrition.match-diagnostics.json"
                                ).path
                        )
                    }
                )
                    .run()

            assertEquals(
                expected = 2,
                actual =
                    result.existingMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.addedMappingCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.unchangedMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.conflictCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.finalMappingCount
            )

            assertEquals(
                expected =
                    originalContent,
                actual =
                    outputMappingFile.readText()
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun supportInitiallyMissingMappingFile() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-mapping-initial-"
            )
                .toFile()

        try {
            val outputMappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            val validationReportFile =
                File(
                    directory,
                    "nutrition.mapping-validation-report.json"
                )

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
                  "entries": []
                }
                """.trimIndent()
                        )
                    }

            val result =
                DefaultNutritionKnowledgeMappingPersistenceStep(
                    representativeValidationFile =
                        representativeValidationFile,
                    outputMappingFile =
                        outputMappingFile,
                    persistMappings = {

                        outputMappingFile.writeMappings(
                            entries =
                                listOf(
                                    MappingFixture(
                                        catalogKey =
                                            "apple",
                                        serverKey =
                                            "apple raw"
                                    )
                                )
                        )

                        WriteValidatedNutritionCatalogServerMappingsResult(
                            requestCount =
                                1,
                            decisionCount =
                                1,
                            diagnosticCount =
                                0,
                            serverKeyCount =
                                100,
                            exactMappingCount =
                                0,
                            acceptedMappingCount =
                                1,
                            rejectedDecisionCount =
                                0,
                            validationStatusCounts =
                                mapOf(
                                    "ACCEPTED" to 1
                                ),
                            outputMappingFile =
                                outputMappingFile.path,
                            validationReportFile =
                                validationReportFile.path,
                            diagnosticsFile =
                                File(
                                    directory,
                                    "nutrition.match-diagnostics.json"
                                ).path
                        )
                    }
                )
                    .run()

            assertEquals(
                expected = 0,
                actual =
                    result.existingMappingCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.addedMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.unchangedMappingCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.finalMappingCount
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    private fun File.writeMappings(
        entries: List<MappingFixture>
    ) {
        parentFile
            ?.mkdirs()

        val mappingsJson =
            entries
                .joinToString(
                    separator = ",\n"
                ) { entry ->

                    """
                    {
                      "catalogKey": "${entry.catalogKey}",
                      "serverArtifact": "nutrition.json",
                      "serverKey": "${entry.serverKey}"
                    }
                    """.trimIndent()
                }

        writeText(
            """
            {
              "version": 1,
              "mappings": [
                $mappingsJson
              ]
            }
            """.trimIndent()
        )
    }

    @Test
    fun preserveRepresentativeMappingsAfterRegularWriterReplacesFile() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-preserve-representative-"
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
                            },
                            {
                              "catalogKey": "fruit yogurt",
                              "serverArtifact": "nutrition.json",
                              "serverKey": "cherry fruit yogurt"
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
                              "decisionType": "REPRESENTATIVE",
                              "accepted": true
                            }
                          ]
                        }
                        """.trimIndent()
                        )
                    }

            val result =
                DefaultNutritionKnowledgeMappingPersistenceStep(
                    outputMappingFile =
                        mappingFile,
                    representativeValidationFile =
                        representativeValidationFile,
                    persistMappings = {

                        /*
                         * Simuliert den regulären Writer, der nur seine
                         * aktuell akzeptierten Decisions schreibt.
                         */
                        mappingFile.writeText(
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
                )
                    .run()

            assertEquals(
                expected = 2,
                actual =
                    result.existingMappingCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.addedMappingCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.finalMappingCount
            )

            val persisted =
                JsonParser.parseString(
                    mappingFile.readText()
                )
                    .asJsonObject["mappings"]
                    .asJsonArray

            assertTrue(
                persisted.any {
                    it.asJsonObject["catalogKey"].asString ==
                            "fruit yogurt"
                }
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    private data class MappingFixture(
        val catalogKey: String,
        val serverKey: String
    )
}