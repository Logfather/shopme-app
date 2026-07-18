package de.shopme.testing.system.tools.knowledge.mapping.catalog.runner

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.runner.WriteValidatedNutritionCatalogServerMappings
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WriteValidatedNutritionCatalogServerMappingsTest {

    @Test
    fun writesOnlyValidatedNutritionMappings() {

        val directory =
            createTempDirectory(
                "validated-nutrition-mappings"
            ).toFile()

        try {
            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )

            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )

            val serverFile =
                File(
                    directory,
                    "nutrition.json"
                )

            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
                )

            val outputMappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            val reportFile =
                File(
                    directory,
                    "nutrition.mapping-validation-report.json"
                )

            writeRequests(
                file = requestFile
            )

            writeDecisions(
                file = decisionFile
            )

            writeServerArtifact(
                file = serverFile
            )

            writeExactMappings(
                file = exactMappingFile
            )

            val result =
                WriteValidatedNutritionCatalogServerMappings(
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    serverArtifactFile =
                        serverFile,
                    exactMappingFile =
                        exactMappingFile,
                    outputMappingFile =
                        outputMappingFile,
                    validationReportFile =
                        reportFile,
                    diagnosticsFile =
                        File(
                            directory,
                            "nutrition.match-diagnostics.json"
                        ),
                    minimumConfidence =
                        0.80,
                    printLine = {}
                ).run()

            assertEquals(
                4,
                result.requestCount
            )

            assertEquals(
                4,
                result.decisionCount
            )

            assertEquals(
                4,
                result.serverKeyCount
            )

            assertEquals(
                1,
                result.exactMappingCount
            )

            assertEquals(
                1,
                result.acceptedMappingCount
            )

            assertEquals(
                3,
                result.rejectedDecisionCount
            )

            assertTrue(
                outputMappingFile.isFile
            )

            assertTrue(
                reportFile.isFile
            )

            val mappingRoot =
                JsonParser.parseString(
                    outputMappingFile.readText()
                ).asJsonObject

            val mappings =
                mappingRoot["mappings"]
                    .asJsonArray

            assertEquals(
                1,
                mappings.size()
            )

            val mapping =
                mappings.single()
                    .asJsonObject

            assertEquals(
                "matjes herring in cream sauce",
                mapping["catalogKey"].asString
            )

            assertEquals(
                "herring fillets in cream sauce",
                mapping["serverKey"].asString
            )

            assertEquals(
                "nutrition.json",
                mapping["sourceArtifact"].asString
            )

            assertEquals(
                "AI_VALIDATED",
                mapping["method"].asString
            )

            val reportRoot =
                JsonParser.parseString(
                    reportFile.readText()
                ).asJsonObject

            assertEquals(
                4,
                reportRoot["validations"]
                    .asJsonArray
                    .size()
            )

        } finally {
            directory.deleteRecursively()
        }
    }


    private fun writeRequests(
        file: File
    ) {

        file.writeText(
            """
            {
              "version": 1,
              "requests": [
                {
                  "catalogKey": "already exact yogurt",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "exact yogurt alternative",
                      "diagnosticScore": 0.95,
                      "sharedTokens": ["yogurt"]
                    }
                  ]
                },
                {
                  "catalogKey": "low confidence yogurt",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "light rice yogurt",
                      "diagnosticScore": 0.90,
                      "sharedTokens": ["rice", "yogurt"]
                    }
                  ]
                },
                {
                  "catalogKey": "matjes herring in cream sauce",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "herring fillets in cream sauce",
                      "diagnosticScore": 0.92,
                      "sharedTokens": ["cream", "herring", "sauce"]
                    }
                  ]
                },
                {
                  "catalogKey": "plain acerola juice",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "mixed acerola fruit juice",
                      "diagnosticScore": 0.82,
                      "sharedTokens": ["acerola", "juice"]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        )
    }


    private fun writeDecisions(
        file: File
    ) {

        file.writeText(
            """
            {
              "version": 1,
              "decisions": [
                {
                  "catalogKey": "already exact yogurt",
                  "serverArtifact": "nutrition.json",
                  "type": "MATCH",
                  "selectedServerKey": "exact yogurt alternative",
                  "confidence": 0.97,
                  "reason": "AI-selected alternative"
                },
                {
                  "catalogKey": "low confidence yogurt",
                  "serverArtifact": "nutrition.json",
                  "type": "MATCH",
                  "selectedServerKey": "light rice yogurt",
                  "confidence": 0.78,
                  "reason": "Related yogurt product"
                },
                {
                  "catalogKey": "matjes herring in cream sauce",
                  "serverArtifact": "nutrition.json",
                  "type": "MATCH",
                  "selectedServerKey": "herring fillets in cream sauce",
                  "confidence": 0.93,
                  "reason": "Same herring preparation"
                },
                {
                  "catalogKey": "plain acerola juice",
                  "serverArtifact": "nutrition.json",
                  "type": "NO_MATCH",
                  "confidence": 0.96,
                  "reason": "No candidate is plain acerola juice"
                }
              ]
            }
            """.trimIndent()
        )
    }


    private fun writeServerArtifact(
        file: File
    ) {

        file.writeText(
            """
            {
              "entries": {
                "exact yogurt alternative": {},
                "herring fillets in cream sauce": {},
                "light rice yogurt": {},
                "mixed acerola fruit juice": {}
              }
            }
            """.trimIndent()
        )
    }


    private fun writeExactMappings(
        file: File
    ) {

        file.writeText(
            """
            {
              "version": 1,
              "mappings": [
                {
                  "catalogKey": "already exact yogurt",
                  "serverKey": "already exact yogurt",
                  "serverArtifact": "nutrition.json",
                  "method": "EXACT"
                }
              ]
            }
            """.trimIndent()
        )
    }
}