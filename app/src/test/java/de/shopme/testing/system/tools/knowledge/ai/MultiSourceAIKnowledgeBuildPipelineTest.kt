package de.shopme.testing.system.tools.knowledge.ai

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilderResolver
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipeline
import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogResultImporter
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MultiSourceAIKnowledgeBuildPipelineTest {

    @Test
    fun run_mergesCandidatesFromMultipleSourcesBeforeUpdatingCatalog() {
        val offBanana = TestKnowledgeCandidates.offBananaWithNutrition()
        val agribalyseBanana = TestKnowledgeCandidates.agribalyseBananaWithCarbon()

        val capturedResults = mutableListOf<AIKnowledgeBuildResult>()

        val pipeline = DefaultAIKnowledgeBuilderPipeline(
            builderResolver = object : AIKnowledgeBuilderResolver {
                override fun resolve(
                    request: AIKnowledgeBuildRequest
                ): AIKnowledgeBuilder {
                    return object : AIKnowledgeBuilder {
                        override fun build(
                            request: AIKnowledgeBuildRequest
                        ): AIKnowledgeBuildResult {
                            return AIKnowledgeBuildResult(
                                candidates = listOf(
                                    offBanana,
                                    agribalyseBanana
                                )
                            )
                        }
                    }
                }
            },
            catalogUpdateWorkflow = object : AIKnowledgeCatalogResultImporter {
                override fun importAIKnowledge(
                    result: AIKnowledgeBuildResult
                ) {
                    capturedResults += result
                }
            }
        )

        val request = AIKnowledgeBuildRequest(
            source = AIKnowledgeSourceInfo(
                type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                name = "Open Food Facts",
                version = "test"
            ),
            inputs = emptyList()
        )

        val result = pipeline.run(request)

        assertEquals(1, result.candidates.size)

        val banana = result.candidates.single()

        assertEquals("banana", banana.canonicalId)
        assertEquals(2, banana.dimensions.size)

        assertTrue(
            banana.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.NUTRITION
            }
        )

        assertTrue(
            banana.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.CARBON
            }
        )

        assertTrue(
            capturedResults.single().candidates.any {
                it.canonicalId == "banana"
            }
        )
    }
}

private object TestKnowledgeCandidates {

    fun offBananaWithNutrition() =
        CanonicalKnowledgeCandidate(
            canonicalId = "banana",
            aliases = setOf("Banana"),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.NUTRITION,
                    payload = mapOf(
                        "caloriesPer100g" to 89
                    )
                )
            ),
            metadata = CandidateMetadata(
                source = "off",
                sourceId = "off-banana",
                confidence = 1.0,
                version = "1"
            )
        )

    fun agribalyseBananaWithCarbon() =
        CanonicalKnowledgeCandidate(
            canonicalId = "banana",
            aliases = setOf("Banane"),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.CARBON,
                    payload = mapOf(
                        "carbonKgCo2EqPerKg" to 0.88
                    )
                )
            ),
            metadata = CandidateMetadata(
                source = "agribalyse",
                sourceId = "agribalyse-banana",
                confidence = 1.0,
                version = "3.2"
            )
        )
}