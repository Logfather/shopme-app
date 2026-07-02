package de.shopme.testing.system.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeCandidateProcessor
import de.shopme.tools.knowledge.compiler.candidate.CanonicalKnowledgeCandidateToPatchOperationMapper
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperationType
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata
import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogUpdateWorkflow
import de.shopme.tools.knowledge.compiler.catalog.DefaultAIKnowledgeCatalogImportWorkflow
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAIKnowledgeCatalogImportWorkflowTest {

    @Test
    fun importAIKnowledgeConvertsResultToPatchAndDelegatesCatalogUpdate() {

        val catalog = listOf(
            catalogItem("apple")
        )

        val candidate = CanonicalKnowledgeCandidate(
            canonicalId = "banana",
            aliases = setOf("banana"),
            dimensions = emptyList(),
            metadata = CandidateMetadata(
                source = "test",
                sourceId = "test-banana",
                confidence = 1.0,
                version = "test"
            )
        )

        val result = AIKnowledgeBuildResult(
            candidates = listOf(candidate)
        )

        val candidateProcessor =
            FakeAIKnowledgeCandidateProcessor(
                batch = KnowledgeImportBatch(
                    candidates = listOf(candidate),
                    metadata = KnowledgeImportBatchMetadata(
                        source = "test",
                        generatedBy = "unit-test",
                        generatedAt = "now"
                    )
                )
            )

        val catalogUpdateWorkflow =
            FakeAIKnowledgeCatalogUpdateWorkflow()

        val workflow =
            DefaultAIKnowledgeCatalogImportWorkflow(
                candidateProcessor = candidateProcessor,
                patchOperationMapper = CanonicalKnowledgeCandidateToPatchOperationMapper(),
                catalogUpdateWorkflow = catalogUpdateWorkflow
            )

        workflow.importAIKnowledge(
            catalog = catalog,
            result = result
        )

        assertEquals(
            catalog,
            catalogUpdateWorkflow.receivedCatalog
        )

        assertEquals(
            listOf("banana"),
            catalogUpdateWorkflow.receivedPatch.operations.map {
                it.canonicalId
            }
        )

        assertEquals(
            FoodsJsonPatchOperationType.ADD,
            catalogUpdateWorkflow.receivedPatch.operations.single().type
        )
    }

    private class FakeAIKnowledgeCandidateProcessor(
        private val batch: KnowledgeImportBatch
    ) : AIKnowledgeCandidateProcessor {

        override fun process(
            result: AIKnowledgeBuildResult
        ): KnowledgeImportBatch {

            return batch
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