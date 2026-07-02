package de.shopme.testing.system.tools.knowledge.compiler.catalog

import com.google.gson.Gson
import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperation
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperationType
import de.shopme.tools.knowledge.compiler.catalog.DefaultFileCatalogUpdateWorkflowFactory
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DefaultFileCatalogUpdateWorkflowFactoryTest {

    @Test
    fun createReturnsWorkflowThatUpdatesCatalogFile() {

        val file = File.createTempFile(
            "catalog",
            ".json"
        )

        try {
            val gson = Gson()

            file.writeText(
                gson.toJson(
                    listOf(
                        catalogItem("tomato"),
                        catalogItem("apple")
                    )
                )
            )

            val workflow = DefaultFileCatalogUpdateWorkflowFactory.create(
                file = file
            )

            workflow.applyPatch(
                patch = FoodsJsonPatch(
                    operations = listOf(
                        FoodsJsonPatchOperation(
                            type = FoodsJsonPatchOperationType.ADD,
                            canonicalId = "banana",
                            candidate = CandidateFoodKnowledgePatch(
                                canonicalId = "banana",
                                aliases = emptySet(),
                                dimensions = emptyList(),
                                metadata = CandidateMetadata(
                                    source = "test",
                                    sourceId = null,
                                    confidence = 1.0,
                                    version = null
                                )
                            )
                        )
                    )
                )
            )

            val updatedCatalog = gson.fromJson(
                file.readText(),
                Array<CatalogItem>::class.java
            ).toList()

            assertEquals(
                listOf(
                    "apple",
                    "banana",
                    "tomato"
                ),
                updatedCatalog.map {
                    it.normalized
                }
            )

            assertTrue(
                file.readText().contains("\n")
            )

        } finally {
            file.delete()
        }
    }

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