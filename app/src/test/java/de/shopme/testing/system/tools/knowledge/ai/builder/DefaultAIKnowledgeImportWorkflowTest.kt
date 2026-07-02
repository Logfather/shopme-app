package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeImportWorkflow
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.compiler.candidate.DefaultKnowledgeImportBatchFactory
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAIKnowledgeImportWorkflowTest {

    @Test
    fun importBuildsKnowledgeImportBatch() {

        val builder = object : AIKnowledgeBuilder {

            override fun build(
                request: AIKnowledgeBuildRequest
            ): AIKnowledgeBuildResult {

                return AIKnowledgeBuildResult(
                    candidates = listOf(
                        CanonicalKnowledgeCandidate(
                            canonicalId = "apple",
                            aliases = setOf("Apple", "Apfel"),
                            dimensions = emptyList(),
                            metadata = CandidateMetadata(
                                source = request.source.name,
                                sourceId = request.inputs.first().sourceId,
                                confidence = 1.0,
                                version = request.source.version
                            )
                        )
                    )
                )
            }
        }

        val workflow = DefaultAIKnowledgeImportWorkflow(
            builder = builder,
            batchFactory = DefaultKnowledgeImportBatchFactory()
        )

        val batch = workflow.import(
            request = AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                    name = "Open Food Facts",
                    version = "1.0"
                ),
                inputs = listOf(
                    RawKnowledgeInput(
                        sourceId = "123",
                        fields = mapOf(
                            "productName" to "Apple"
                        )
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

        assertEquals(1, batch.candidates.size)
        assertEquals("apple", batch.candidates.first().canonicalId)
        assertEquals("open_food_facts", batch.metadata.source)
        assertEquals("test", batch.metadata.generatedBy)
    }
}