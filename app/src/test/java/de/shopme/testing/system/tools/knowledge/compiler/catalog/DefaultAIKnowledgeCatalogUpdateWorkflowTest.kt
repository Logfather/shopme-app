package de.shopme.testing.system.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.DefaultFoodsJsonPatchApplier
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchApplier
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperation
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperationType
import de.shopme.tools.knowledge.compiler.catalog.CatalogWriter
import de.shopme.tools.knowledge.compiler.catalog.DefaultAIKnowledgeCatalogUpdateWorkflow
import de.shopme.tools.knowledge.compiler.catalog.InMemoryCatalogWriter
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAIKnowledgeCatalogUpdateWorkflowTest {

    @Test
    fun updateCatalogWritesUpdatedCatalog() {

        val writer = InMemoryCatalogWriter()

        val workflow = DefaultAIKnowledgeCatalogUpdateWorkflow(
            patchApplier = DefaultFoodsJsonPatchApplier(),
            catalogWriter = writer
        )

        workflow.updateCatalog(
            catalog = listOf(
                catalogItemWithNormalized("apple")
            ),
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

        assertEquals(
            listOf(
                "apple",
                "banana"
            ),
            writer.readWrittenCatalog().map {
                it.normalized
            }
        )
    }

    @Test
    fun updateCatalogAppliesPatchAndWritesUpdatedCatalog() {

        val catalog =
            listOf(
                catalogItem("apple")
            )

        val updatedCatalog =
            listOf(
                catalogItem("apple"),
                catalogItem("banana")
            )

        val patch =
            FoodsJsonPatch(
                operations = emptyList()
            )

        val patchApplier =
            FakeFoodsJsonPatchApplier(
                updatedCatalog = updatedCatalog
            )

        val writer =
            FakeCatalogWriter()

        val workflow =
            DefaultAIKnowledgeCatalogUpdateWorkflow(
                patchApplier = patchApplier,
                catalogWriter = writer
            )

        workflow.updateCatalog(
            catalog = catalog,
            patch = patch
        )

        assertEquals(
            catalog,
            patchApplier.receivedCatalog
        )

        assertEquals(
            patch,
            patchApplier.receivedPatch
        )

        assertEquals(
            updatedCatalog,
            writer.writtenCatalog
        )
    }

    private class FakeFoodsJsonPatchApplier(
        private val updatedCatalog: List<CatalogItem>
    ) : FoodsJsonPatchApplier {

        lateinit var receivedCatalog: List<CatalogItem>
        lateinit var receivedPatch: FoodsJsonPatch

        override fun apply(
            catalog: List<CatalogItem>,
            patch: FoodsJsonPatch
        ): List<CatalogItem> {

            receivedCatalog = catalog
            receivedPatch = patch

            return updatedCatalog
        }
    }

    private class FakeCatalogWriter : CatalogWriter {

        lateinit var writtenCatalog: List<CatalogItem>

        override fun write(
            catalog: List<CatalogItem>
        ) {
            writtenCatalog = catalog
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


    private fun catalogItemWithNormalized(
        normalized: String
    ): CatalogItem =
        CatalogItem(
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