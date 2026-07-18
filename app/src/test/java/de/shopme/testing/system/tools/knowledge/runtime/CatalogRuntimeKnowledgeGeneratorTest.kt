package de.shopme.testing.system.tools.knowledge.runtime

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.runtime.CatalogRuntimeKnowledgeGenerator
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CatalogRuntimeKnowledgeGeneratorTest {

    @Test
    fun appliesExactAndValidatedCatalogServerMappings() {

        val directory =
            createTempDirectory(
                prefix =
                    "catalog-runtime-generator"
            ).toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )

            val serverDirectory =
                File(
                    directory,
                    "server"
                )

            val runtimeDirectory =
                File(
                    directory,
                    "runtime"
                )

            val mappingFile =
                File(
                    directory,
                    "catalog-server.mappings.json"
                )

            check(serverDirectory.mkdirs())

            writeCatalog(
                file = catalogFile
            )

            writeServerArtifact(
                file =
                    File(
                        serverDirectory,
                        "nutrition.json"
                    )
            )

            writeMappings(
                file = mappingFile
            )

            val report =
                CatalogRuntimeKnowledgeGenerator(
                    printLine = {}
                ).generate(
                    catalogFile =
                        catalogFile,
                    serverArtifactDirectory =
                        serverDirectory,
                    runtimeArtifactDirectory =
                        runtimeDirectory,
                    catalogServerMappingFile =
                        mappingFile
                )

            val nutritionReport =
                report.artifacts
                    .single {
                        it.artifact == "nutrition.json"
                    }

            assertTrue(
                nutritionReport.mappedMatchCount > 0,
                "Expected validated AI mappings to add nutrition runtime entries"
            )

            assertTrue(
                nutritionReport.runtimeEntryCount >
                        nutritionReport.exactMatchCount,
                "Nutrition runtime coverage must exceed exact-only coverage"
            )

            assertEquals(
                nutritionReport.exactMatchCount +
                        nutritionReport.mappedMatchCount,
                nutritionReport.runtimeEntryCount
            )

            assertEquals(
                4,
                report.catalogKeyCount
            )

            assertEquals(
                4,
                report.mappingCount
            )

            val artifactReport =
                report.artifacts.single()

            assertEquals(
                "nutrition.json",
                artifactReport.artifact
            )

            assertEquals(
                4,
                artifactReport.serverEntryCount
            )

            assertEquals(
                1,
                artifactReport.exactMatchCount
            )

            assertEquals(
                2,
                artifactReport.mappedMatchCount
            )

            assertEquals(
                3,
                artifactReport.runtimeEntryCount
            )

            assertEquals(
                1,
                artifactReport.missingCount
            )

            val runtimeFile =
                File(
                    runtimeDirectory,
                    "nutrition.json"
                )

            assertTrue(
                runtimeFile.isFile
            )

            val entries =
                JsonParser.parseString(
                    runtimeFile.readText()
                )
                    .asJsonObject["entries"]
                    .asJsonObject

            /*
             * Exact Match:
             * Der vorhandene Catalog-Key bleibt erhalten.
             */
            assertTrue(
                entries.has(
                    "apple yogurt"
                )
            )

            assertEquals(
                100,
                entries["apple yogurt"]
                    .asJsonObject["energy"]
                    .asInt
            )

            /*
             * Mapping:
             * Catalog-Key wird als Runtime-Key geschrieben.
             */
            assertTrue(
                entries.has(
                    "matjes herring in cream sauce"
                )
            )

            assertEquals(
                230,
                entries[
                    "matjes herring in cream sauce"
                ]
                    .asJsonObject["energy"]
                    .asInt
            )

            assertFalse(
                entries.has(
                    "herring fillets in cream sauce"
                )
            )

            /*
             * Exact Match hat Vorrang vor Mapping.
             *
             * Das Mapping für apple yogurt zeigt absichtlich
             * auf apple yogurt light. Trotzdem muss der exakte
             * apple-yogurt-Wert verwendet werden.
             */
            assertEquals(
                100,
                entries["apple yogurt"]
                    .asJsonObject["energy"]
                    .asInt
            )

            /*
             * Zweites gültiges Mapping.
             */
            assertTrue(
                entries.has(
                    "rice yogurt"
                )
            )

            assertEquals(
                80,
                entries["rice yogurt"]
                    .asJsonObject["energy"]
                    .asInt
            )

            /*
             * Mapping-Ziel existiert nicht im Artefakt.
             */
            assertFalse(
                entries.has(
                    "unknown catalog food"
                )
            )

        } finally {
            directory.deleteRecursively()
        }
    }


    @Test
    fun keepsExactOnlyBehaviorWhenMappingFileIsMissing() {

        val directory =
            createTempDirectory(
                prefix =
                    "catalog-runtime-generator-no-mapping"
            ).toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )

            val serverDirectory =
                File(
                    directory,
                    "server"
                )

            val runtimeDirectory =
                File(
                    directory,
                    "runtime"
                )

            check(serverDirectory.mkdirs())

            writeCatalog(
                file = catalogFile
            )

            writeServerArtifact(
                file =
                    File(
                        serverDirectory,
                        "nutrition.json"
                    )
            )

            val report =
                CatalogRuntimeKnowledgeGenerator(
                    printLine = {}
                ).generate(
                    catalogFile =
                        catalogFile,
                    serverArtifactDirectory =
                        serverDirectory,
                    runtimeArtifactDirectory =
                        runtimeDirectory,
                    catalogServerMappingFile =
                        File(
                            directory,
                            "missing-mappings.json"
                        )
                )

            val artifactReport =
                report.artifacts.single()

            assertEquals(
                1,
                artifactReport.exactMatchCount
            )

            assertEquals(
                0,
                artifactReport.mappedMatchCount
            )

            assertEquals(
                1,
                artifactReport.runtimeEntryCount
            )

            val entries =
                JsonParser.parseString(
                    File(
                        runtimeDirectory,
                        "nutrition.json"
                    ).readText()
                )
                    .asJsonObject["entries"]
                    .asJsonObject

            assertEquals(
                setOf(
                    "apple yogurt"
                ),
                entries.keySet()
            )

        } finally {
            directory.deleteRecursively()
        }
    }


    private fun writeCatalog(
        file: File
    ) {

        file.writeText(
            """
            [
              {
                "normalizedEnglish": "apple yogurt"
              },
              {
                "normalizedEnglish": "matjes herring in cream sauce"
              },
              {
                "normalizedEnglish": "rice yogurt"
              },
              {
                "normalizedEnglish": "unknown catalog food"
              }
            ]
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
                "apple yogurt": {
                  "energy": 100
                },
                "apple yogurt light": {
                  "energy": 70
                },
                "herring fillets in cream sauce": {
                  "energy": 230
                },
                "light rice yogurt": {
                  "energy": 80
                }
              }
            }
            """.trimIndent()
        )
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
                  "catalogKey": "apple yogurt",
                  "serverKey": "apple yogurt light",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.95,
                  "reason": "Test exact precedence"
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
                },
                {
                  "catalogKey": "unknown catalog food",
                  "serverKey": "missing server food",
                  "sourceArtifact": "nutrition.json",
                  "method": "AI_VALIDATED",
                  "confidence": 0.99,
                  "reason": "Missing target test"
                }
              ]
            }
            """.trimIndent()
        )
    }
}