package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.data.KnowledgeDataDirectories
import de.shopme.tools.knowledge.importer.off.JsonlGzipOFFImportReader
import de.shopme.tools.knowledge.importer.off.ai.FakeOFFAIExtractionClient
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionBatch
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionInputMapper
import org.junit.Test
import java.io.File
import kotlin.test.DefaultAsserter.assertTrue
import kotlin.test.assertEquals

class OFFAIExtractionSmokeTest {

    @Test
    fun extractsKnowledgeImportBatchFromOFFPreview() {

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
                .take(20)
                .map(mapper::map)
                .toList()

        val batch =
            OFFAIExtractionBatch(
                source = "off-preview-50k",
                sourceVersion = "test",
                products = products
            )

        val result =
            FakeOFFAIExtractionClient()
                .extract(batch)

        assertEquals(
            "off-preview-50k",
            result.metadata.source
        )

        assertEquals(
            "fake-off-ai-client",
            result.metadata.generatedBy
        )

        assertTrue(
            "Expected fake AI client to create candidates",
            result.candidates.isNotEmpty()
        )

        assertTrue(
            "Expected all candidates to have canonical ids",
            result.candidates.all { candidate ->
                candidate.canonicalId.isNotBlank()
            }
        )
    }
}