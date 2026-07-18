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
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RecordedCatalogKnowledgeMatcherFixtureTest {

    @Test
    fun matchesCatalogKnowledgeUsingRecordedFixture() {

        val recordedProvider =
            RecordedFixtureAIProvider(
                fixturePath =
                    "/knowledge/catalog/match/" +
                            "semi-skimmed-milk-match-response.json"
            )

        val matcher =
            AIProviderCatalogKnowledgeMatcher(
                aiProvider =
                    recordedProvider,
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
                        diagnosticScore =
                            0.94,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    ),
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed pasteurized",
                        diagnosticScore =
                            0.81,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
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

        assertTrue(
            decision.isMatch
        )

        assertEquals(
            request.catalogKey,
            decision.catalogKey
        )

        assertEquals(
            request.serverArtifact,
            decision.serverArtifact
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
            "The catalog food represents semi-skimmed UHT milk.",
            decision.reason
        )

        assertEquals(
            1,
            recordedProvider.requests.size
        )

        val providerRequest =
            recordedProvider.requests.single()

        val normalizedSystemPrompt =
            providerRequest.systemPrompt
                .lowercase()
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim()

        assertTrue(
            normalizedSystemPrompt.contains(
                "food identity matcher"
            ),
            "System prompt must describe the food identity matcher role"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "return match only when one candidate clearly represents the same food"
            ),
            "System prompt must define when MATCH is allowed"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "never invent"
            ) &&
                    normalizedSystemPrompt.contains(
                        "server key"
                    ),
            "System prompt must prohibit invented server keys"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "selectedserverkey must be copied exactly"
            ),
            "System prompt must require exact candidate keys"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "for no_match, selectedserverkey must be null"
            ),
            "System prompt must require null selectedServerKey for NO_MATCH"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "confidence expresses confidence in the complete decision"
            ),
            "System prompt must define confidence for the complete decision"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "for no_match, confidence means certainty that none of the supplied candidates is an acceptable match"
            ),
            "System prompt must define NO_MATCH confidence"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "a confident no_match must therefore use a high confidence value"
            ),
            "System prompt must require high confidence for a confident NO_MATCH"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "do not set confidence to 0 merely because no candidate was selected"
            ),
            "System prompt must not equate NO_MATCH with zero confidence"
        )

        assertTrue(
            normalizedSystemPrompt.contains(
                "return only one valid json object"
            ),
            "System prompt must require a single JSON object"
        )

        assertContains(
            providerRequest.userPrompt,
            "semi skimmed milk"
        )

        assertContains(
            providerRequest.userPrompt,
            "environmental_impact.json"
        )

        assertContains(
            providerRequest.userPrompt,
            "milk semi skimmed uht"
        )

        assertContains(
            providerRequest.userPrompt,
            "milk semi skimmed pasteurized"
        )

        assertContains(
            providerRequest.userPrompt,
            "diagnosticScore=0.94"
        )

        assertContains(
            providerRequest.userPrompt,
            "diagnosticScore=0.81"
        )
    }


    private class RecordedFixtureAIProvider(
        private val fixturePath: String
    ) : AIProvider {

        val requests =
            mutableListOf<AIProviderRequest>()


        override fun complete(
            request: AIProviderRequest
        ): AIProviderResponse {

            requests += request

            return AIProviderResponse(
                content =
                    readFixture(
                        path = fixturePath
                    )
            )
        }


        private fun readFixture(
            path: String
        ): String {

            val normalizedPath =
                if (path.startsWith("/")) {
                    path
                } else {
                    "/$path"
                }

            val stream =
                RecordedCatalogKnowledgeMatcherFixtureTest::class.java
                    .getResourceAsStream(
                        normalizedPath
                    )

            requireNotNull(stream) {
                "Recorded AI fixture not found: $normalizedPath"
            }

            return stream
                .bufferedReader()
                .use {
                    it.readText()
                }
                .trim()
        }
    }
}