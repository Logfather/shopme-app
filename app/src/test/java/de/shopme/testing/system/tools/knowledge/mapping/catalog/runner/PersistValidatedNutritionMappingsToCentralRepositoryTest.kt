package de.shopme.testing.system.tools.knowledge.mapping.catalog.runner

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.runner.PersistValidatedNutritionMappingsToCentralRepository
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PersistValidatedNutritionMappingsToCentralRepositoryTest {

    @Test
    fun copiesValidatedNutritionMappingsAndMergesCentralRepository() {

        val directory =
            createTempDirectory(
                prefix =
                    "central-nutrition-mapping-repository"
            ).toFile()

        try {
            val mappingDirectory =
                File(
                    directory,
                    "mappings"
                )

            val validatedDirectory =
                File(
                    mappingDirectory,
                    "validated"
                )

            val sourceMappingFile =
                File(
                    mappingDirectory,
                    "nutrition-source.mappings.json"
                )

            val centralMappingFile =
                File(
                    mappingDirectory,
                    "catalog-server.mappings.json"
                )

            val validatedNutritionFile =
                File(
                    validatedDirectory,
                    "nutrition.validated-mappings.json"
                )

            val conflictReportFile =
                File(
                    directory,
                    "reports/catalog-server-mapping-conflicts.json"
                )

            val mergeReportFile =
                File(
                    directory,
                    "reports/catalog-server-mapping-merge-report.json"
                )

            check(mappingDirectory.mkdirs())

            writeExistingCentralMappings(
                file = centralMappingFile
            )

            writeValidatedNutritionMappings(
                file = sourceMappingFile
            )

            val result =
                PersistValidatedNutritionMappingsToCentralRepository(
                    validatedNutritionMappingFile =
                        sourceMappingFile,
                    centralValidatedMappingFile =
                        validatedNutritionFile,
                    existingCentralMappingFile =
                        centralMappingFile,
                    validatedMappingDirectory =
                        validatedDirectory,
                    conflictReportFile =
                        conflictReportFile,
                    mergeReportFile =
                        mergeReportFile,
                    printLine = {}
                ).run()

            assertTrue(
                validatedNutritionFile.isFile
            )

            assertTrue(
                centralMappingFile.isFile
            )

            assertTrue(
                conflictReportFile.isFile
            )

            assertTrue(
                mergeReportFile.isFile
            )

            assertEquals(
                1,
                result.addedMappingCount
            )

            assertEquals(
                1,
                result.unchangedMappingCount
            )

            assertEquals(
                1,
                result.conflictCount
            )

            assertEquals(
                3,
                result.totalMappingCount
            )

            val validatedRoot =
                JsonParser
                    .parseString(
                        validatedNutritionFile.readText()
                    )
                    .asJsonObject

            assertEquals(
                3,
                validatedRoot["mappings"]
                    .asJsonArray
                    .size()
            )

            val centralRoot =
                JsonParser
                    .parseString(
                        centralMappingFile.readText()
                    )
                    .asJsonObject

            val centralMappings =
                centralRoot["mappings"]
                    .asJsonArray

            assertEquals(
                3,
                centralMappings.size()
            )

            val mappingsByCatalogKey =
                centralMappings
                    .associateBy {
                        it.asJsonObject[
                            "catalogKey"
                        ].asString
                    }

            assertEquals(
                "apple yogurt",
                mappingsByCatalogKey
                    .getValue(
                        "apple yogurt"
                    )
                    .asJsonObject[
                    "serverKey"
                ]
                    .asString
            )

            assertEquals(
                "herring fillets in cream sauce",
                mappingsByCatalogKey
                    .getValue(
                        "matjes herring in cream sauce"
                    )
                    .asJsonObject[
                    "serverKey"
                ]
                    .asString
            )

            assertEquals(
                "light rice yogurt",
                mappingsByCatalogKey
                    .getValue(
                        "rice yogurt"
                    )
                    .asJsonObject[
                    "serverKey"
                ]
                    .asString
            )

        } finally {
            directory.deleteRecursively()
        }
    }


    private fun writeExistingCentralMappings(
        file: File
    ) {

        file.writeText(
            """
            {
              "version": 1,
              "mappings": [
                {
                  "catalogKey": "apple yogurt",
                  "serverKey": "apple yogurt",
                  "sourceArtifact": "nutrition.json",
                  "method": "EXACT",
                  "confidence": 1.0,
                  "reason": "Exact mapping"
                },
                {
                  "catalogKey": "matjes herring in cream sauce",
                  "serverKey": "herring fillets in cream sauce",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.93,
                  "reason": "Existing validated mapping"
                }
              ]
            }
            """.trimIndent()
        )
    }


    private fun writeValidatedNutritionMappings(
        file: File
    ) {

        file.writeText(
            """
            {
              "version": 1,
              "mappings": [
                {
                  "catalogKey": "apple yogurt",
                  "serverKey": "apple yogurt light",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.91,
                  "reason": "Conflicting mapping"
                },
                {
                  "catalogKey": "matjes herring in cream sauce",
                  "serverKey": "herring fillets in cream sauce",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.93,
                  "reason": "Same mapping"
                },
                {
                  "catalogKey": "rice yogurt",
                  "serverKey": "light rice yogurt",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.89,
                  "reason": "New validated mapping"
                }
              ]
            }
            """.trimIndent()
        )
    }
}