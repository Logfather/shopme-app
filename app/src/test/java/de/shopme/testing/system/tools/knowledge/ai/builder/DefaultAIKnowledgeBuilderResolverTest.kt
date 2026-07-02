package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuilder
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.DefaultAIKnowledgeBuilderResolver
import org.junit.Assert.assertSame
import org.junit.Test

class DefaultAIKnowledgeBuilderResolverTest {

    @Test
    fun resolveReturnsBuilderForSourceName() {

        val builder =
            FakeAIKnowledgeBuilder()

        val resolver =
            DefaultAIKnowledgeBuilderResolver(
                buildersBySourceType = mapOf(
                    AIKnowledgeSourceType.OPEN_FOOD_FACTS to builder
                )
            )

        val resolvedBuilder =
            resolver.resolve(
                testRequest(
                    sourceType = AIKnowledgeSourceType.OPEN_FOOD_FACTS
                )
            )

        assertSame(
            builder,
            resolvedBuilder
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun resolveThrowsWhenNoBuilderIsRegisteredForSourceName() {

        val resolver =
            DefaultAIKnowledgeBuilderResolver(
                buildersBySourceType = emptyMap()
            )
        resolver.resolve(
            testRequest(
                sourceType = AIKnowledgeSourceType.CUSTOM
            )
        )
    }

    private class FakeAIKnowledgeBuilder : AIKnowledgeBuilder {

        override fun build(
            request: AIKnowledgeBuildRequest
        ): AIKnowledgeBuildResult {

            return AIKnowledgeBuildResult(
                candidates = emptyList()
            )
        }
    }

    companion object {

        private fun testRequest(
            sourceType: AIKnowledgeSourceType
        ): AIKnowledgeBuildRequest {

            return AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = sourceType,
                    name = sourceType.name,
                    version = "1.0"
                ),
                inputs = emptyList()
            )
        }
    }
}