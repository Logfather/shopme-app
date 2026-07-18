package de.shopme.testing.system.tools.knowledge.ai

import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseCanonicalCandidateBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderResolver
import de.shopme.tools.knowledge.ai.builder.off.DeterministicOFFCandidateBuilder
import org.junit.Assert.assertTrue
import kotlin.test.Test

class DefaultAIKnowledgeBuilderResolverTest {

    @Test
    fun resolvesOpenFoodFactsBuilder() {
        val resolver = DefaultAIKnowledgeBuilderResolver(
            buildersBySourceType = mapOf(
                AIKnowledgeSourceType.OPEN_FOOD_FACTS to DeterministicOFFCandidateBuilder(),
                AIKnowledgeSourceType.AGRIBALYSE to AgribalyseCanonicalCandidateBuilder()
            )
        )

        val builder = resolver.resolve(
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                    name = "OFF"
                ),
                inputs = emptyList()
            )
        )

        assertTrue(builder is DeterministicOFFCandidateBuilder)
    }

    @Test
    fun resolvesAgribalyseBuilder() {
        val resolver = DefaultAIKnowledgeBuilderResolver(
            buildersBySourceType = mapOf(
                AIKnowledgeSourceType.OPEN_FOOD_FACTS to DeterministicOFFCandidateBuilder(),
                AIKnowledgeSourceType.AGRIBALYSE to AgribalyseCanonicalCandidateBuilder()
            )
        )

        val builder = resolver.resolve(
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.AGRIBALYSE,
                    name = "Agribalyse"
                ),
                inputs = emptyList()
            )
        )

        assertTrue(builder is AgribalyseCanonicalCandidateBuilder)
    }

}