package de.shopme.testing.system.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.CatalogImportWorkflow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CatalogImportWorkflowTest {

    @Test
    fun importsCandidatesAndMergesCatalogItems() {

        val importFile =
            File.createTempFile(
                "knowledge-import",
                ".json"
            )

        importFile.writeText(
            """
            {
              "metadata": {
                "source": "test-source",
                "generatedBy": "test-ai",
                "generatedAt": "2026-06-28",
                "promptVersion": "test-v1"
              },
              "candidates": [
                {
                  "id": "aepfel",
                  "names": {
                    "canonical": "aepfel",
                    "aliases": []
                  },
                  "knowledge": {
                    "carbonImpact": {
                      "reference": "aepfel",
                      "source": "carbon_impact",
                      "value": "LOW"
                    },
                    "glycemicIndex": {
                      "reference": "aepfel",
                      "source": "glycemic_index",
                      "value": "UNKNOWN"
                    }
                  }
                }
              ]
            }
            """.trimIndent()
        )

        val existingItems =
            emptyList<CatalogItem>()

        val result =
            CatalogImportWorkflow()
                .import(
                    existingItems = existingItems,
                    importFile = importFile
                )

        assertTrue(result.isSuccess)
        assertNotNull(result.mergeResult)

        val mergeResult =
            result.mergeResult!!

        assertEquals(1, mergeResult.summary.importedItems)
        assertEquals(1, mergeResult.summary.addedItems)
        assertEquals(0, mergeResult.summary.updatedItems)
        assertEquals(1, mergeResult.summary.mergedItems)

        val importedItem =
            mergeResult.items.single()

        assertEquals("aepfel", importedItem.normalized)
        assertEquals("aepfel", importedItem.itemname)

        assertEquals(
            "LOW",
            importedItem.knowledge?.carbonImpact?.value
        )

        assertEquals(
            "UNKNOWN",
            importedItem.knowledge?.glycemicIndex?.value
        )

        importFile.delete()
    }

    @Test
    fun importsCandidatesAndUpdatesExistingCatalogItems() {

        val importFile =
            File.createTempFile(
                "knowledge-import-update",
                ".json"
            )

        importFile.writeText(
            """
        {
          "metadata": {
            "source": "test-source",
            "generatedBy": "test-ai",
            "generatedAt": "2026-06-28",
            "promptVersion": "test-v1"
          },
          "candidates": [
            {
              "id": "aepfel",
              "names": {
                "canonical": "aepfel",
                "aliases": ["apfel"]
              },
              "knowledge": {
                "carbonImpact": {
                  "reference": "aepfel",
                  "source": "carbon_impact",
                  "value": "LOW"
                },
                "glycemicIndex": {
                  "reference": "aepfel",
                  "source": "glycemic_index",
                  "value": "UNKNOWN"
                }
              }
            }
          ]
        }
        """.trimIndent()
        )

        val existingItems =
            listOf(
                CatalogItem(
                    itemname = "aepfel",
                    category = "Obst",
                    production = "",
                    normalized = "aepfel",
                    plural = "aepfel",
                    colloquial = listOf("aeppel"),
                    phonetic_tokens = emptyList(),
                    autocomplete_tokens = emptyList()
                )
            )

        val result =
            CatalogImportWorkflow()
                .import(
                    existingItems = existingItems,
                    importFile = importFile
                )

        assertTrue(result.isSuccess)
        assertNotNull(result.mergeResult)

        val mergeResult =
            result.mergeResult!!

        assertEquals(1, mergeResult.summary.importedItems)
        assertEquals(0, mergeResult.summary.addedItems)
        assertEquals(1, mergeResult.summary.updatedItems)
        assertEquals(1, mergeResult.summary.mergedItems)

        val updatedItem =
            mergeResult.items.single()

        assertEquals("aepfel", updatedItem.normalized)
        assertEquals("Obst", updatedItem.category)

        assertEquals(
            listOf("aeppel", "apfel"),
            updatedItem.colloquial
        )

        assertEquals(
            "LOW",
            updatedItem.knowledge?.carbonImpact?.value
        )

        assertEquals(
            "UNKNOWN",
            updatedItem.knowledge?.glycemicIndex?.value
        )

        importFile.delete()
    }

    @Test
    fun rejectsInvalidCatalogImport() {

        val importFile =
            File.createTempFile(
                "knowledge-import-invalid",
                ".json"
            )

        importFile.writeText(
            """
        {
          "metadata": {
            "source": "test-source",
            "generatedBy": "test-ai",
            "generatedAt": "2026-06-28"
          },
          "candidates": [
            {
              "id": "",
              "names": {
                "canonical": "",
                "aliases": []
              },
              "knowledge": {}
            }
          ]
        }
        """.trimIndent()
        )

        val result =
            CatalogImportWorkflow()
                .import(
                    existingItems = emptyList(),
                    importFile = importFile
                )

        assertEquals(false, result.isSuccess)
        assertTrue(result.errors.isNotEmpty())
        assertEquals(null, result.mergeResult)

        importFile.delete()
    }
}