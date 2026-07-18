package de.shopme.testing.system.tools.knowledge.mapping.catalog.runner

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.runner.MergeValidatedCatalogServerKnowledgeMappings
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MergeValidatedCatalogServerKnowledgeMappingsTest {

    @Test
    fun persistsMergedMappingsAndConflictReports() {

        val directory =
            createTempDirectory(
                prefix =
                    "catalog-server-mapping-merge"
            ).toFile()

        try {
            val validatedDirectory =
                File(
                    directory,
                    "validated"
                )

            check(
                validatedDirectory.mkdirs()
            )

            val existingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            val outputFile =
                File(
                    directory,
                    "merged/catalog-server.mappings.json"
                )

            val conflictFile =
                File(
                    directory,
                    "reports/catalog-server-mapping-conflicts.json"
                )

            val reportFile =
                File(
                    directory,
                    "reports/catalog-server-mapping-merge-report.json"
                )

            existingFile.writeText(
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
                    }
                  ]
                }
                """.trimIndent()
            )

            File(
                validatedDirectory,
                "nutrition.validated-mappings.json"
            ).writeText(
                """
                {
                  "version": 1,
                  "mappings": [
                    {
                      "catalogKey": "rice yogurt",
                      "serverKey": "light rice yogurt",
                      "sourceArtifact": "nutrition.json",
                      "method": "AI_VALIDATED",
                      "confidence": 0.84,
                      "reason": "Validated nutrition mapping"
                    }
                  ]
                }
                """.trimIndent()
            )

            File(
                validatedDirectory,
                "ingredients.validated-mappings.json"
            ).writeText(
                """
                {
                  "version": 1,
                  "mappings": [
                    {
                      "catalogKey": "apple yogurt",
                      "serverKey": "apple yogurt light",
                      "sourceArtifact": "ingredients.json",
                      "method": "AI_VALIDATED",
                      "confidence": 0.91,
                      "reason": "Conflicting ingredients mapping"
                    },
                    {
                      "catalogKey": "rice yogurt",
                      "serverKey": "light rice yogurt",
                      "sourceArtifact": "ingredients.json",
                      "method": "AI_VALIDATED",
                      "confidence": 0.89,
                      "reason": "Validated ingredients mapping"
                    }
                  ]
                }
                """.trimIndent()
            )

            val report =
                MergeValidatedCatalogServerKnowledgeMappings(
                    existingMappingFile =
                        existingFile,
                    validatedMappingDirectory =
                        validatedDirectory,
                    outputMappingFile =
                        outputFile,
                    conflictReportFile =
                        conflictFile,
                    mergeReportFile =
                        reportFile,
                    printLine = {}
                ).run()

            assertEquals(
                1,
                report.existingMappingCount
            )

            assertEquals(
                3,
                report.incomingMappingCount
            )

            assertEquals(
                1,
                report.addedMappingCount
            )

            assertEquals(
                1,
                report.unchangedMappingCount
            )

            assertEquals(
                1,
                report.conflictCount
            )

            assertEquals(
                2,
                report.totalMappingCount
            )

            assertTrue(outputFile.isFile)
            assertTrue(conflictFile.isFile)
            assertTrue(reportFile.isFile)

            val outputRoot =
                JsonParser.parseString(
                    outputFile.readText()
                ).asJsonObject

            val outputMappings =
                outputRoot["mappings"]
                    .asJsonArray

            assertEquals(
                2,
                outputMappings.size()
            )

            val catalogKeys =
                outputMappings.map {
                    it.asJsonObject["catalogKey"]
                        .asString
                }

            assertEquals(
                listOf(
                    "apple yogurt",
                    "rice yogurt"
                ),
                catalogKeys
            )

            val conflictRoot =
                JsonParser.parseString(
                    conflictFile.readText()
                ).asJsonObject

            assertEquals(
                1,
                conflictRoot["conflicts"]
                    .asJsonArray
                    .size()
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}