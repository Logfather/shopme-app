package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProvider
import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.mapping.catalog.AIProviderCatalogKnowledgeMatcher
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.DefaultCatalogKnowledgeMatchPromptFactory
import de.shopme.tools.knowledge.mapping.catalog.DefaultCatalogKnowledgeMatchResponseParser
import kotlin.test.Test
import kotlin.test.assertEquals

class AIProviderCatalogKnowledgeMatcherTest {

    @Test
    fun matchesCatalogKnowledgeRequest() {

        val matcher =
            AIProviderCatalogKnowledgeMatcher(
                aiProvider = FakeMatchAIProvider(),
                promptFactory =
                    DefaultCatalogKnowledgeMatchPromptFactory(),
                responseParser =
                    DefaultCatalogKnowledgeMatchResponseParser()
            )

        val request =
            CatalogKnowledgeMatchRequest(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                candidates = listOf(
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed uht",
                        diagnosticScore = 0.94,
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
                        diagnosticScore = 0.81,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    )
                )
            )

        val decision =
            matcher.match(request)

        assertEquals(
            CatalogKnowledgeMatchDecisionType.MATCH,
            decision.type
        )

        assertEquals(
            "milk semi skimmed uht",
            decision.selectedServerKey
        )

        assertEquals(
            0.98,
            decision.confidence
        )

        assertEquals(
            "Semantic match",
            decision.reason
        )
    }


    private class FakeMatchAIProvider :
        AIProvider {

        override fun complete(
            request: AIProviderRequest
        ): AIProviderResponse {

            return AIProviderResponse(
                content =
                    """
                    {
                      "match": true,
                      "selectedServerKey":"milk semi skimmed uht",
                      "confidence":0.98,
                      "reason":"Semantic match"
                    }
                    """.trimIndent()
            )
        }
    }
}