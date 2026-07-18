package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.openai.OpenAIProvider
import de.shopme.tools.knowledge.ai.openai.OpenAIProviderConfig
import de.shopme.tools.knowledge.ai.openai.RealOpenAIHttpClient
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.OpenAICatalogKnowledgeMatcherFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RunSingleOpenAICatalogKnowledgeMatchTest {

    @Test
    fun matchSingleCatalogKnowledgeRequestWithOpenAI() {

        val environment =
            System.getenv()

        val enabled =
            environment[
                RUN_TEST_ENVIRONMENT_VARIABLE
            ]
                ?.trim()
                ?.equals(
                    other = "true",
                    ignoreCase = true
                )
                ?: false

        if (!enabled) {
            println(
                "Real OpenAI catalog match test skipped. " +
                        "Set $RUN_TEST_ENVIRONMENT_VARIABLE=true."
            )

            return
        }

        val openAIConfig =
            OpenAIProviderConfig
                .fromEnvironment()

        val providerConfig =
            AIProviderConfig(
                providerName =
                    "openai",
                model =
                    openAIConfig.model,
                apiKey =
                    openAIConfig.apiKey,
                endpoint =
                    openAIConfig.endpoint,
                temperature =
                    1.0
            )

        val openAIProvider =
            OpenAIProvider(
                config =
                    providerConfig,
                httpClient =
                    RealOpenAIHttpClient(
                        config =
                            providerConfig
                    )
            )

        val matcher =
            OpenAICatalogKnowledgeMatcherFactory(
                openAIProvider =
                    openAIProvider
            ).create()

        val request =
            createRequest()

        val decision =
            matcher.match(
                request =
                    request
            )

        printResult(
            model =
                providerConfig.model,
            request =
                request,
            decision =
                decision
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
            CatalogKnowledgeMatchDecisionType.MATCH,
            decision.type,
            "Expected OpenAI to select a matching candidate, " +
                    "but decision was ${decision.type}: " +
                    decision.reason
        )

        val selectedServerKey =
            assertNotNull(
                decision.selectedServerKey,
                "MATCH decision must contain selectedServerKey"
            )

        assertTrue(
            request.candidates.any { candidate ->
                candidate.serverKey ==
                        selectedServerKey
            },
            "OpenAI selected a server key that was not supplied " +
                    "as candidate: $selectedServerKey"
        )

        assertEquals(
            EXPECTED_SERVER_KEY,
            selectedServerKey,
            "OpenAI selected an unexpected food identity"
        )

        assertTrue(
            decision.confidence in 0.0..1.0,
            "Confidence must be between 0.0 and 1.0, " +
                    "but was ${decision.confidence}"
        )

        assertTrue(
            decision.confidence >=
                    MINIMUM_EXPECTED_CONFIDENCE,
            "Expected confidence >= " +
                    "$MINIMUM_EXPECTED_CONFIDENCE, " +
                    "but was ${decision.confidence}"
        )

        assertTrue(
            decision.reason.isNotBlank(),
            "OpenAI decision reason must not be blank"
        )
    }


    private fun createRequest():
            CatalogKnowledgeMatchRequest =
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


    private fun printResult(
        model: String,
        request: CatalogKnowledgeMatchRequest,
        decision: CatalogKnowledgeMatchDecision
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OPENAI CATALOG KNOWLEDGE MATCH")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Model       : $model")
        println("Catalog key : ${request.catalogKey}")
        println("Artifact    : ${request.serverArtifact}")
        println("Candidates  :")

        request.candidates
            .forEachIndexed { index, candidate ->

                println(
                    "  ${index + 1}. " +
                            "${candidate.serverKey} " +
                            "(diagnosticScore=" +
                            "${candidate.diagnosticScore})"
                )
            }

        println("Decision    : ${decision.type}")
        println("Selected    : ${decision.selectedServerKey}")
        println("Confidence  : ${decision.confidence}")
        println("Reason      : ${decision.reason}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }


    companion object {

        private const val RUN_TEST_ENVIRONMENT_VARIABLE =
            "RUN_OPENAI_CATALOG_MATCH_TEST"

        private const val EXPECTED_SERVER_KEY =
            "milk semi skimmed uht"

        private const val MINIMUM_EXPECTED_CONFIDENCE =
            0.80
    }
}