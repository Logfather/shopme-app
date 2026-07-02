package de.shopme.testing.system.tools.knowledge.compiler.catalog

import com.google.gson.Gson
import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.DefaultFoodsJsonPatchApplier
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperation
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperationType
import de.shopme.tools.knowledge.compiler.catalog.DefaultAIKnowledgeCatalogUpdateWorkflow
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogReader
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogUpdateWorkflow
import de.shopme.tools.knowledge.compiler.catalog.GsonCatalogJsonDeserializer
import de.shopme.tools.knowledge.compiler.catalog.GsonCatalogJsonSerializer
import de.shopme.tools.knowledge.compiler.catalog.JsonCatalogWriter
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class FileCatalogUpdateWorkflowIntegrationTest {

    @Test
    fun updateCatalogReadsAppliesPatchAndWritesUpdatedCatalog() {

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

            val reader = FileCatalogReader(
                deserializer = GsonCatalogJsonDeserializer(gson),
                inputFile = file
            )

            val writer = JsonCatalogWriter(
                serializer = GsonCatalogJsonSerializer(gson),
                outputFile = file
            )

            val updateWorkflow = DefaultAIKnowledgeCatalogUpdateWorkflow(
                patchApplier = DefaultFoodsJsonPatchApplier(),
                catalogWriter = writer
            )

            val workflow = FileCatalogUpdateWorkflow(
                reader = reader,
                updateWorkflow = updateWorkflow
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

            val updatedCatalog = reader.read()

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