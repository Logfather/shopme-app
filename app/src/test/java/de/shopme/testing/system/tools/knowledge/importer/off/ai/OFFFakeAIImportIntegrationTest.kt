package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.data.KnowledgeDataDirectories
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportProcessor
import de.shopme.tools.knowledge.importer.off.JsonlGzipOFFImportReader
import de.shopme.tools.knowledge.importer.off.ai.FakeOFFAIExtractionClient
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionBatch
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionInputMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OFFFakeAIImportIntegrationTest {

    @Test
    fun importsOFFPreviewThroughFakeAIIntoCatalogItems() {

        val file =
            File(
                KnowledgeDataDirectories.openFoodFactsPreview,
                "off-products-preview-50k.jsonl.gz"
            )

        assertTrue(
            "Expected OFF preview file to exist at ${file.path}",
            file.exists()
        )

        val mapper =
            OFFAIExtractionInputMapper()

        val products =
            JsonlGzipOFFImportReader()
                .read(file)
                .map(mapper::map)
                .filter { input ->
                    !input.categories.isNullOrBlank()
                }
                .take(20)
                .toList()

        val batch =
            OFFAIExtractionBatch(
                source = "off-preview-50k",
                sourceVersion = "test",
                products = products
            )

        val importBatch =
            FakeOFFAIExtractionClient()
                .extract(batch)

        val processResult =
            KnowledgeImportProcessor()
                .process(importBatch)

        assertTrue(
            "Expected OFF preview sample to contain products with categories",
            products.isNotEmpty()
        )

        assertTrue(
            "Expected import processing to succeed, errors:\n" +
                    processResult.errors.joinToString("\n"),
            processResult.isSuccess
        )

        assertTrue(
            "Expected accepted candidates",
            processResult.acceptedCandidates.isNotEmpty()
        )

        assertTrue(
            "Expected catalog items",
            processResult.catalogItems.isNotEmpty()
        )

        assertEquals(
            processResult.acceptedCandidates.size,
            processResult.catalogItems.size
        )

        assertTrue(
            "Expected all catalog items to have normalized names",
            processResult.catalogItems.all { item ->
                item.normalized.isNotBlank()
            }
        )

        val itemWithTaxonomy =
            processResult.catalogItems.first { item ->
                item.knowledge?.taxonomy?.value?.isNotBlank() == true
            }

        assertEquals(
            "off-preview-50k",
            itemWithTaxonomy.knowledge?.taxonomy?.source
        )

        assertEquals(
            itemWithTaxonomy.normalized,
            itemWithTaxonomy.knowledge?.taxonomy?.reference
        )
    }
}