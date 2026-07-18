package de.shopme.testing.system.tools.knowledge.mapping.catalog.runner

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.runner.EvaluateCatalogServerMappingsAgainstKnowledgeArtifacts
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluateCatalogServerMappingsAgainstKnowledgeArtifactsTest {

    @Test
    fun evaluatesReusableMappingsAcrossAllServerArtifacts() {

        val directory =
            createTempDirectory(
                prefix =
                    "catalog-server-mapping-coverage"
            ).toFile()

        try {
            val mappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            val serverDirectory =
                File(
                    directory,
                    "server"
                )

            val reportFile =
                File(
                    directory,
                    "catalog-server-mapping-" +
                            "artifact-coverage.json"
                )

            check(serverDirectory.mkdirs())

            writeMappings(
                file = mappingFile
            )

            writeNutritionArtifact(
                file =
                    File(
                        serverDirectory,
                        "nutrition.json"
                    )
            )

            writeIngredientsArtifact(
                file =
                    File(
                        serverDirectory,
                        "ingredients.json"
                    )
            )

            writeEnvironmentalArtifact(
                file =
                    File(
                        serverDirectory,
                        "environmental_impact.json"
                    )
            )

            val report =
                EvaluateCatalogServerMappingsAgainstKnowledgeArtifacts(
                    mappingFile =
                        mappingFile,
                    serverArtifactDirectory =
                        serverDirectory,
                    reportFile =
                        reportFile,
                    printLine = {}
                ).run()

            assertEquals(
                3,
                report.mappingCount
            )

            assertEquals(
                3,
                report.artifactCount
            )

            val byArtifact =
                report.artifacts
                    .associateBy {
                        it.artifact
                    }

            val nutrition =
                requireNotNull(
                    byArtifact["nutrition.json"]
                )

            assertEquals(
                3,
                nutrition.serverKeyCount
            )

            assertEquals(
                3,
                nutrition.reusableMappingCount
            )

            assertEquals(
                0,
                nutrition.missingMappingCount
            )

            val ingredients =
                requireNotNull(
                    byArtifact["ingredients.json"]
                )

            assertEquals(
                2,
                ingredients.reusableMappingCount
            )

            assertEquals(
                1,
                ingredients.missingMappingCount
            )

            assertEquals(
                listOf(
                    "matjes herring in cream sauce",
                    "rice yogurt"
                ),
                ingredients.reusableCatalogKeys
            )

            val environmental =
                requireNotNull(
                    byArtifact[
                        "environmental_impact.json"
                    ]
                )

            assertEquals(
                1,
                environmental.reusableMappingCount
            )

            assertEquals(
                2,
                environmental.missingMappingCount
            )

            assertEquals(
                listOf(
                    "matjes herring in cream sauce"
                ),
                environmental.reusableCatalogKeys
            )

            assertTrue(
                reportFile.isFile
            )

            val persistedReport =
                JsonParser.parseString(
                    reportFile.readText()
                ).asJsonObject

            assertEquals(
                3,
                persistedReport["mappingCount"]
                    .asInt
            )

            assertEquals(
                3,
                persistedReport["artifacts"]
                    .asJsonArray
                    .size()
            )

        } finally {
            directory.deleteRecursively()
        }
    }


    private fun writeMappings(
        file: File
    ) {

        file.writeText(
            """
            {
              "version": 1,
              "mappings": [
                {
                  "catalogKey": "frozen vegetable lasagna",
                  "serverKey": "vegetable lasagna, vegetable",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.86,
                  "reason": "Same food"
                },
                {
                  "catalogKey": "matjes herring in cream sauce",
                  "serverKey": "herring fillets in cream sauce",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.93,
                  "reason": "Same food"
                },
                {
                  "catalogKey": "rice yogurt",
                  "serverKey": "light rice yogurt",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.84,
                  "reason": "Accepted test mapping"
                }
              ]
            }
            """.trimIndent()
        )
    }


    private fun writeNutritionArtifact(
        file: File
    ) {

        file.writeText(
            """
            {
              "entries": {
                "herring fillets in cream sauce": {},
                "light rice yogurt": {},
                "vegetable lasagna, vegetable": {}
              }
            }
            """.trimIndent()
        )
    }


    private fun writeIngredientsArtifact(
        file: File
    ) {

        file.writeText(
            """
            {
              "entries": {
                "herring fillets in cream sauce": {},
                "light rice yogurt": {}
              }
            }
            """.trimIndent()
        )
    }


    private fun writeEnvironmentalArtifact(
        file: File
    ) {

        file.writeText(
            """
            {
              "entries": {
                "herring fillets in cream sauce": {}
              }
            }
            """.trimIndent()
        )
    }
}