package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.compiler.candidate.DefaultKnowledgeImportBatchFactory
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultKnowledgeImportBatchFactoryTest {

    @Test
    fun createImportBatchFromAIKnowledgeBuildResult() {

        val result = AIKnowledgeBuildResult(
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
            )
        )

        val metadata = KnowledgeImportBatchMetadata(
            source = "open_food_facts",
            generatedBy = "test",
            generatedAt = "2026-06-30T00:00:00Z",
            promptVersion = null
        )

        val batch = DefaultKnowledgeImportBatchFactory()
            .create(
                result = result,
                metadata = metadata
            )

        assertEquals(1, batch.candidates.size)
        assertEquals("apple", batch.candidates.first().canonicalId)
        assertEquals("open_food_facts", batch.metadata.source)
        assertEquals("test", batch.metadata.generatedBy)
    }
}