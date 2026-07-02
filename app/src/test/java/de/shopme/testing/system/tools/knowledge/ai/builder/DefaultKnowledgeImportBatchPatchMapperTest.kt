package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.compiler.candidate.DefaultKnowledgeImportBatchPatchMapper
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatchOperationType
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultKnowledgeImportBatchPatchMapperTest {

    @Test
    fun mapImportBatchToFoodsJsonPatch() {

        val batch = KnowledgeImportBatch(
            candidates = listOf(
                CanonicalKnowledgeCandidate(
                    canonicalId = "apple",
                    aliases = setOf("Apple", "Apfel"),
                    dimensions = emptyList(),
                    metadata = CandidateMetadata(
                        source = "open_food_facts",
                        sourceId = "123",
                        confidence = 1.0,
                        version = "manual-v1"
                    )
                )
            ),
            metadata = KnowledgeImportBatchMetadata(
                source = "open_food_facts",
                generatedBy = "test",
                generatedAt = "2026-06-30T00:00:00Z",
                promptVersion = null
            )
        )

        val patch = DefaultKnowledgeImportBatchPatchMapper()
            .map(batch)

        assertEquals(1, patch.operations.size)
        assertEquals(FoodsJsonPatchOperationType.ADD, patch.operations.first().type)
        assertEquals("apple", patch.operations.first().canonicalId)
        assertEquals("apple", patch.operations.first().candidate.canonicalId)
        assertEquals(setOf("Apple", "Apfel"), patch.operations.first().candidate.aliases)
    }
}