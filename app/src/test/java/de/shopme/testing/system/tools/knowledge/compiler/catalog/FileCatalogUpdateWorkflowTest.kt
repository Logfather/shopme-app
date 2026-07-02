package de.shopme.testing.system.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogUpdateWorkflow
import de.shopme.tools.knowledge.compiler.catalog.CatalogJsonDeserializer
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogReader
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogUpdateWorkflow
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class FileCatalogUpdateWorkflowTest {

    @Test
    fun updateCatalogReadsCatalogAndDelegatesPatch() {

        val file = File.createTempFile(
            "catalog",
            ".json"
        )

        try {
            file.writeText("TEST_JSON")

            val reader = FileCatalogReader(
                deserializer = FakeCatalogJsonDeserializer(),
                inputFile = file
            )

            val updateWorkflow = FakeAIKnowledgeCatalogUpdateWorkflow()

            val workflow = FileCatalogUpdateWorkflow(
                reader = reader,
                updateWorkflow = updateWorkflow
            )

            val patch = FoodsJsonPatch(
                operations = emptyList()
            )

            workflow.applyPatch(
                patch = patch
            )

            assertEquals(
                listOf("apple"),
                updateWorkflow.receivedCatalog.map {
                    it.normalized
                }
            )

            assertEquals(
                patch,
                updateWorkflow.receivedPatch
            )

        } finally {
            file.delete()
        }
    }

    private class FakeCatalogJsonDeserializer :
        CatalogJsonDeserializer {

        override fun deserialize(
            json: String
        ): List<CatalogItem> {

            assertEquals(
                "TEST_JSON",
                json
            )

            return listOf(
                catalogItem("apple")
            )
        }
    }

    private class FakeAIKnowledgeCatalogUpdateWorkflow :
        AIKnowledgeCatalogUpdateWorkflow {

        lateinit var receivedCatalog: List<CatalogItem>
        lateinit var receivedPatch: FoodsJsonPatch

        override fun updateCatalog(
            catalog: List<CatalogItem>,
            patch: FoodsJsonPatch
        ) {

            receivedCatalog = catalog
            receivedPatch = patch
        }
    }

    companion object {

        private fun catalogItem(
            normalized: String
        ): CatalogItem {

            return CatalogItem(
                itemname = normalized,
                category = "",
                production = "",
                normalized = normalized,
                plural = "",
                colloquial = emptyList(),
                phonetic_tokens = emptyList(),
                autocomplete_tokens = emptyList()
            )
        }
    }
}