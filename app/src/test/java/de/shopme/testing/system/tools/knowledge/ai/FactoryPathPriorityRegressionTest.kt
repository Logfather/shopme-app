package de.shopme.testing.system.tools.knowledge.ai

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderPipeline
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderResolver
import de.shopme.tools.knowledge.compiler.catalog.AIKnowledgeCatalogResultImporter
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FactoryPathPriorityRegressionTest {

    @Test
    fun run_keepsSourcePriorityWhenFactoryPathProducesOffAndAgribalyseCandidates() {
        val capturedResults =
            mutableListOf<AIKnowledgeBuildResult>()

        val pipeline =
            DefaultAIKnowledgeBuilderPipeline(
                builderResolver = DefaultAIKnowledgeBuilderResolver(
                    buildersBySourceType = mapOf(
                        AIKnowledgeSourceType.OPEN_FOOD_FACTS to
                                MultiSourceCandidateStubBuilder()
                    )
                ),
                catalogUpdateWorkflow = object : AIKnowledgeCatalogResultImporter {
                    override fun importAIKnowledge(
                        result: AIKnowledgeBuildResult
                    ) {
                        capturedResults += result
                    }
                }
            )

        val result =
            pipeline.run(
                request = AIKnowledgeBuildRequest(
                    source = AIKnowledgeSourceInfo(
                        type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                        name = "factory-path-priority-regression",
                        version = "test"
                    ),
                    inputs = emptyList()
                )
            )

        assertEquals(1, result.candidates.size)
        assertEquals(result, capturedResults.single())

        val banana =
            result.candidates.single()

        assertEquals("banana", banana.canonicalId)

        val nutrition =
            banana.dimensions.single {
                it.dimension == KnowledgeDimensionCandidateType.NUTRITION
            }

        val carbon =
            banana.dimensions.single {
                it.dimension == KnowledgeDimensionCandidateType.CARBON
            }

        val water =
            banana.dimensions.single {
                it.dimension == KnowledgeDimensionCandidateType.WATER
            }

        val packaging =
            banana.dimensions.single {
                it.dimension == KnowledgeDimensionCandidateType.PACKAGING
            }

        assertEquals(
            mapOf("caloriesPer100g" to 89),
            nutrition.payload
        )

        assertEquals(
            mapOf("carbonKgCo2EqPerKg" to 0.88),
            carbon.payload
        )

        assertEquals(
            mapOf("waterM3DeprivationPerKg" to 0.12),
            water.payload
        )

        assertEquals(
            mapOf("packaging" to "plastic_wrap"),
            packaging.payload
        )

        assertTrue("Banana" in banana.aliases)
        assertTrue("Banane" in banana.aliases)
    }
}

private class MultiSourceCandidateStubBuilder : AIKnowledgeBuilder {

    override fun build(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuildResult {
        return AIKnowledgeBuildResult(
            candidates = listOf(
                offBananaCandidate(),
                agribalyseBananaCandidate()
            )
        )
    }

    private fun offBananaCandidate() =
        CanonicalKnowledgeCandidate(
            canonicalId = "banana",
            aliases = setOf("Banana"),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.NUTRITION,
                    payload = mapOf(
                        "caloriesPer100g" to 89
                    )
                ),
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.PACKAGING,
                    payload = mapOf(
                        "packaging" to "plastic_wrap"
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

    private fun agribalyseBananaCandidate() =
        CanonicalKnowledgeCandidate(
            canonicalId = "banana",
            aliases = setOf("Banane"),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.NUTRITION,
                    payload = mapOf(
                        "caloriesPer100g" to 92
                    )
                ),
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.CARBON,
                    payload = mapOf(
                        "carbonKgCo2EqPerKg" to 0.88
                    )
                ),
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.WATER,
                    payload = mapOf(
                        "waterM3DeprivationPerKg" to 0.12
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