package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class AIKnowledgeBuilderContractTest {

    @Test
    fun buildReturnsCanonicalKnowledgeCandidates() {

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

        val result = builder.build(
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    name = "open_food_facts",
                    version = "manual-v1"
                ),
                inputs = listOf(
                    RawKnowledgeInput(
                        sourceId = "123",
                        fields = mapOf(
                            "productName" to "Apple",
                            "productNameDe" to "Apfel"
                        )
                    )
                )
            )
        )

        assertEquals(1, result.candidates.size)
        assertEquals("apple", result.candidates.first().canonicalId)
        assertEquals("open_food_facts", result.candidates.first().metadata.source)
        assertEquals("123", result.candidates.first().metadata.sourceId)
    }
}