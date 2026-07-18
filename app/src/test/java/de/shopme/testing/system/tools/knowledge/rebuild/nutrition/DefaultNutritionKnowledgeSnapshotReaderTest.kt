package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.rebuild.nutrition.adapter.DefaultNutritionKnowledgeSnapshotReader
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultNutritionKnowledgeSnapshotReaderTest {

    @Test
    fun countExactMappedRuntimeAndMissingNutritionCoverage() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-snapshot-"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "items": [
                                {
                                  "normalizedEnglish": "apple"
                                },
                                {
                                  "normalizedEnglish": "banana"
                                },
                                {
                                  "normalizedEnglish": "cherry yogurt"
                                },
                                {
                                  "normalizedEnglish": "unknown meal"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
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
                      "serverKey": "apple"
                    },
                    {
                      "catalogKey": "banana",
                      "serverArtifact": "nutrition.json",
                      "serverKey": "banana"
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
                                  "catalogKey": "cherry yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "lowfat cherry yogurt"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val runtimeNutritionFile =
                File(
                    directory,
                    "runtime.nutrition.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "entries": [
                                {
                                  "catalogKey": "apple"
                                },
                                {
                                  "catalogKey": "banana"
                                },
                                {
                                  "catalogKey": "cherry yogurt"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val snapshot =
                DefaultNutritionKnowledgeSnapshotReader(
                    catalogFile =
                        catalogFile,
                    exactMappingFile =
                        exactMappingFile,
                    runtimeNutritionFile =
                        runtimeNutritionFile,
                    mappingFile =
                        mappingFile
                )
                    .read()

            assertEquals(
                expected = 1,
                actual =
                    snapshot.mappingCount
            )

            assertEquals(
                expected = 4,
                actual =
                    snapshot.catalogItemCount
            )

            assertEquals(
                expected = 2,
                actual =
                    snapshot.exactMatchCount
            )

            assertEquals(
                expected = 1,
                actual =
                    snapshot.mappedMatchCount
            )

            assertEquals(
                expected = 3,
                actual =
                    snapshot.runtimeEntryCount
            )

            assertEquals(
                expected = 3,
                actual =
                    snapshot.coveredCatalogItemCount
            )

            assertEquals(
                expected = 1,
                actual =
                    snapshot.missingCatalogItemCount
            )

            assertEquals(
                expected = 0.75,
                actual =
                    snapshot.coverage
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun doNotCountMappingTwiceWhenCatalogKeyIsExact() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-snapshot-exact-mapped-"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )
                    .apply {
                        writeText(
                            """
                            [
                              {
                                "normalizedEnglish": "apple"
                              }
                            ]
                            """.trimIndent()
                        )
                    }



            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
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
                      "serverKey": "apple"
                    },
                    {
                      "catalogKey": "banana",
                      "serverArtifact": "nutrition.json",
                      "serverKey": "banana"
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
                              "mappings": [
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "serverKey": "apple"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val runtimeNutritionFile =
                File(
                    directory,
                    "runtime.nutrition.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "apple": {
                                "energy": 50
                              }
                            }
                            """.trimIndent()
                        )
                    }

            val snapshot =
                DefaultNutritionKnowledgeSnapshotReader(
                    catalogFile =
                        catalogFile,
                    exactMappingFile =
                        exactMappingFile,
                    runtimeNutritionFile =
                        runtimeNutritionFile,
                    mappingFile =
                        mappingFile
                )
                    .read()

            assertEquals(
                expected = 1,
                actual =
                    snapshot.mappingCount
            )

            assertEquals(
                expected = 1,
                actual =
                    snapshot.exactMatchCount
            )

            assertEquals(
                expected = 0,
                actual =
                    snapshot.mappedMatchCount
            )

            assertEquals(
                expected = 1,
                actual =
                    snapshot.coveredCatalogItemCount
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun rejectRuntimeArtifactThatDiffersFromCalculatedCoverage() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-snapshot-inconsistent-"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )
                    .apply {
                        writeText(
                            """
                            [
                              {
                                "normalizedEnglish": "apple"
                              }
                            ]
                            """.trimIndent()
                        )
                    }



            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
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
                      "serverKey": "apple"
                    },
                    {
                      "catalogKey": "banana",
                      "serverArtifact": "nutrition.json",
                      "serverKey": "banana"
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
                              "mappings": []
                            }
                            """.trimIndent()
                        )
                    }

            val runtimeNutritionFile =
                File(
                    directory,
                    "runtime.nutrition.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "entries": []
                            }
                            """.trimIndent()
                        )
                    }

            val exception =
                kotlin.test.assertFailsWith<
                        IllegalArgumentException
                        > {

                    DefaultNutritionKnowledgeSnapshotReader(
                        catalogFile =
                            catalogFile,
                        exactMappingFile =
                            exactMappingFile,
                        runtimeNutritionFile =
                            runtimeNutritionFile,
                        mappingFile =
                            mappingFile
                    )
                        .read()
                }

            assertTrue(
                exception.message
                    .orEmpty()
                    .contains(
                        "Runtime nutrition entry count differs from " +
                                "calculated coverage"
                    )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun deduplicateRepeatedNormalizedCatalogKeys() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-snapshot-duplicate-catalog-"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )
                    .apply {
                        writeText(
                            """
                        {
                          "items": [
                            {
                              "normalizedEnglish": "Almond Drink"
                            },
                            {
                              "normalizedEnglish": "almond-drink"
                            },
                            {
                              "normalizedEnglish": "almond drink"
                            },
                            {
                              "normalizedEnglish": "apple"
                            }
                          ]
                        }
                        """.trimIndent()
                        )
                    }

            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
                )
                    .apply {
                        writeText(
                            """
                        {
                          "version": 1,
                          "mappings": [
                            {
                              "catalogKey": "almond drink",
                              "serverArtifact": "nutrition.json",
                              "serverKey": "almond drink"
                            },
                            {
                              "catalogKey": "apple",
                              "serverArtifact": "nutrition.json",
                              "serverKey": "apple"
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
                          "mappings": []
                        }
                        """.trimIndent()
                        )
                    }

            val runtimeNutritionFile =
                File(
                    directory,
                    "runtime.nutrition.json"
                )
                    .apply {
                        writeText(
                            """
                        {
                          "entries": [
                            {
                              "catalogKey": "almond drink"
                            },
                            {
                              "catalogKey": "apple"
                            }
                          ]
                        }
                        """.trimIndent()
                        )
                    }

            val snapshot =
                DefaultNutritionKnowledgeSnapshotReader(
                    catalogFile =
                        catalogFile,
                    exactMappingFile =
                        exactMappingFile,
                    runtimeNutritionFile =
                        runtimeNutritionFile,
                    mappingFile =
                        mappingFile
                )
                    .read()

            assertEquals(
                expected = 2,
                actual =
                    snapshot.catalogItemCount
            )

            assertEquals(
                expected = 2,
                actual =
                    snapshot.exactMatchCount
            )

            assertEquals(
                expected = 0,
                actual =
                    snapshot.mappedMatchCount
            )

            assertEquals(
                expected = 2,
                actual =
                    snapshot.runtimeEntryCount
            )

            assertEquals(
                expected = 2,
                actual =
                    snapshot.coveredCatalogItemCount
            )

            assertEquals(
                expected = 0,
                actual =
                    snapshot.missingCatalogItemCount
            )

            assertEquals(
                expected = 1.0,
                actual =
                    snapshot.coverage
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun acceptMappedRuntimeEntryStoredUnderServerKey() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-snapshot-server-key-runtime-"
            )
                .toFile()

        try {
            val catalogFile =
                File(
                    directory,
                    "catalog.json"
                )
                    .apply {
                        writeText(
                            """
                        {
                          "items": [
                            {
                              "normalizedEnglish": "cherry yogurt"
                            }
                          ]
                        }
                        """.trimIndent()
                        )
                    }



            val exactMappingFile =
                File(
                    directory,
                    "nutrition.mappings.json"
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
                      "serverKey": "apple"
                    },
                    {
                      "catalogKey": "banana",
                      "serverArtifact": "nutrition.json",
                      "serverKey": "banana"
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
                          "mappings": [
                            {
                              "catalogKey": "cherry yogurt",
                              "serverArtifact": "nutrition.json",
                              "serverKey": "lowfat black cherry yogurt"
                            }
                          ]
                        }
                        """.trimIndent()
                        )
                    }

            /*
             * Der Runtime-Eintrag ist unter dem Server Key gespeichert,
             * nicht unter "cherry yogurt".
             */
            val runtimeNutritionFile =
                File(
                    directory,
                    "runtime.nutrition.json"
                )
                    .apply {
                        writeText(
                            """
                        {
                          "lowfat black cherry yogurt": {
                            "energy": 80
                          }
                        }
                        """.trimIndent()
                        )
                    }

            val snapshot =
                DefaultNutritionKnowledgeSnapshotReader(
                    catalogFile =
                        catalogFile,
                    exactMappingFile =
                        exactMappingFile,
                    runtimeNutritionFile =
                        runtimeNutritionFile,
                    mappingFile =
                        mappingFile
                )
                    .read()

            assertEquals(
                expected = 0,
                actual =
                    snapshot.exactMatchCount
            )

            assertEquals(
                expected = 1,
                actual =
                    snapshot.mappedMatchCount
            )

            assertEquals(
                expected = 1,
                actual =
                    snapshot.runtimeEntryCount
            )

            assertEquals(
                expected = 1,
                actual =
                    snapshot.coveredCatalogItemCount
            )

            assertEquals(
                expected = 0,
                actual =
                    snapshot.missingCatalogItemCount
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}