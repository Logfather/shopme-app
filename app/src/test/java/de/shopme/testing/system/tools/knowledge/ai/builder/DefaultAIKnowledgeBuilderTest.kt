package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeCandidateExtractor
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAIKnowledgeBuilderTest {

    @Test
    fun buildUsesExtractorAndReturnsCandidates() {

        val builder = DefaultAIKnowledgeBuilder(
            extractor = object : AIKnowledgeCandidateExtractor {

                override fun extract(
                    request: AIKnowledgeBuildRequest
                ): List<CanonicalKnowledgeCandidate> {

                    return listOf(
                        CanonicalKnowledgeCandidate(
                            canonicalId = "apple",
                            aliases = setOf("Apple"),
                            dimensions = emptyList(),
                            metadata = CandidateMetadata(
                                source = request.source.name,
                                sourceId = request.inputs.first().sourceId,
                                confidence = 1.0,
                                version = request.source.version
                            )
                        )
                    )
                }
            }
        )

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
                            "productName" to "Apple"
                        )
                    )
                )
            )
        )

        assertEquals(1, result.candidates.size)
        assertEquals("apple", result.candidates.first().canonicalId)
    }

    @Test(expected = IllegalArgumentException::class)
    fun buildFailsForEmptyInputs() {

        val builder = DefaultAIKnowledgeBuilder(
            extractor = object : AIKnowledgeCandidateExtractor {

                override fun extract(
                    request: AIKnowledgeBuildRequest
                ): List<CanonicalKnowledgeCandidate> {

                    return emptyList()
                }
            }
        )

        builder.build(
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    name = "open_food_facts"
                ),
                inputs = emptyList()
            )
        )
    }
}