package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProvider
import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.OpenAICatalogKnowledgeMatcherFactory
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class OpenAICatalogKnowledgeMatcherFactoryTest {

    @Test
    fun wiresCatalogKnowledgeMatcherToProvidedOpenAIProvider() {

        val provider =
            RecordingOpenAIProvider()

        val matcher =
            OpenAICatalogKnowledgeMatcherFactory(
                openAIProvider = provider
            ).create()

        val request =
            CatalogKnowledgeMatchRequest(
                catalogKey =
                    "semi skimmed uht milk",
                serverArtifact =
                    "environmental_impact.json",
                candidates = listOf(
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed uht",
                        diagnosticScore =
                            0.95,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed",
                            "uht"
                        )
                    ),
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed pasteurized",
                        diagnosticScore =
                            0.82,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    ),
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk whole uht",
                        diagnosticScore =
                            0.62,
                        sharedTokens = listOf(
                            "milk",
                            "uht"
                        )
                    )
                )
            )

        val decision =
            matcher.match(
                request = request
            )

        assertEquals(
            CatalogKnowledgeMatchDecisionType.MATCH,
            decision.type
        )

        assertEquals(
            "semi skimmed uht milk",
            decision.catalogKey
        )

        assertEquals(
            "environmental_impact.json",
            decision.serverArtifact
        )

        assertEquals(
            "milk semi skimmed uht",
            decision.selectedServerKey
        )

        assertEquals(
            0.99,
            decision.confidence
        )

        assertEquals(
            "The candidate represents semi-skimmed UHT milk.",
            decision.reason
        )

        val recordedRequest =
            assertNotNull(
                provider.recordedRequest
            )

        val normalizedSystemPrompt =
            recordedRequest.systemPrompt
                .lowercase()
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim()

        assertTrue(
            normalizedSystemPrompt.contains(
                "return match only when one candidate clearly represents the same food"
            )
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "never invent"
            ) &&
                    normalizedSystemPrompt.contains(
                        "server key"
                    )
        )

        assertContains(
            recordedRequest.userPrompt,
            "semi skimmed uht milk"
        )

        assertContains(
            recordedRequest.userPrompt,
            "environmental_impact.json"
        )

        assertContains(
            recordedRequest.userPrompt,
            "milk semi skimmed uht"
        )

        assertContains(
            recordedRequest.userPrompt,
            "milk semi skimmed pasteurized"
        )

        assertContains(
            recordedRequest.userPrompt,
            "milk whole uht"
        )
    }


    private class RecordingOpenAIProvider :
        AIProvider {

        var recordedRequest:
                AIProviderRequest? =
            null
            private set


        override fun complete(
            request: AIProviderRequest
        ): AIProviderResponse {

            recordedRequest =
                request

            return AIProviderResponse(
                content =
                    """
                    {
                      "match": true,
                      "selectedServerKey": "milk semi skimmed uht",
                      "confidence": 0.99,
                      "reason": "The candidate represents semi-skimmed UHT milk."
                    }
                    """.trimIndent()
            )
        }
    }
}