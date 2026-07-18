package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.DefaultCatalogKnowledgeMatchPromptFactory
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultCatalogKnowledgeMatchPromptFactoryTest {

    @Test
    fun explainsConfidenceForMatchAndNoMatchDecisions() {

        val providerRequest =
            DefaultCatalogKnowledgeMatchPromptFactory()
                .create(
                    request = createRequest()
                )

        val normalizedPrompt =
            providerRequest.systemPrompt
                .lowercase()
                .replace(
                    Regex("\\s+"),
                    " "
                )
                .trim()

        assertTrue(
            normalizedPrompt.contains(
                "confidence expresses confidence in the complete decision"
            ),
            "Prompt must define confidence as confidence in the complete decision"
        )

        assertTrue(
            normalizedPrompt.contains(
                "for match"
            ),
            "Prompt must explain MATCH confidence"
        )

        assertTrue(
            normalizedPrompt.contains(
                "selected candidate"
            ) &&
                    normalizedPrompt.contains(
                        "same food"
                    ),
            "Prompt must define MATCH confidence as certainty about food identity"
        )

        assertTrue(
            normalizedPrompt.contains(
                "for no_match"
            ),
            "Prompt must explain NO_MATCH confidence"
        )

        assertTrue(
            normalizedPrompt.contains(
                "none of the supplied candidates"
            ) &&
                    normalizedPrompt.contains(
                        "acceptable match"
                    ),
            "Prompt must define NO_MATCH confidence as certainty that no candidate is acceptable"
        )

        assertTrue(
            normalizedPrompt.contains(
                "confident no_match"
            ) &&
                    normalizedPrompt.contains(
                        "high confidence"
                    ),
            "Prompt must require high confidence for a confident NO_MATCH"
        )

        assertTrue(
            normalizedPrompt.contains(
                "do not set confidence to 0"
            ),
            "Prompt must prevent confidence 0 from being used merely for NO_MATCH"
        )

        assertTrue(
            normalizedPrompt.contains(
                "no candidate was selected"
            ),
            "Prompt must explain why an unselected candidate does not imply confidence 0"
        )
    }


    @Test
    fun instructsModelNotToInventServerKeys() {

        val providerRequest =
            DefaultCatalogKnowledgeMatchPromptFactory()
                .create(
                    request =
                        createRequest()
                )

        assertContains(
            providerRequest.systemPrompt,
            "Never invent"
        )

        assertContains(
            providerRequest.systemPrompt,
            "server key"
        )

        assertContains(
            providerRequest.systemPrompt,
            "selectedServerKey"
        )

        assertContains(
            providerRequest.systemPrompt,
            "copied exactly"
        )

        assertContains(
            providerRequest.systemPrompt,
            "For NO_MATCH"
        )

        assertContains(
            providerRequest.systemPrompt,
            "must be null"
        )
    }


    @Test
    fun includesCatalogArtifactAndOrderedCandidates() {

        val providerRequest =
            DefaultCatalogKnowledgeMatchPromptFactory()
                .create(
                    request =
                        createRequest()
                )

        assertContains(
            providerRequest.userPrompt,
            "rice yogurt"
        )

        assertContains(
            providerRequest.userPrompt,
            "nutrition.json"
        )

        assertContains(
            providerRequest.userPrompt,
            "light rice yogurt"
        )

        assertContains(
            providerRequest.userPrompt,
            "rice cakes yogurt coating"
        )

        val firstCandidateIndex =
            providerRequest.userPrompt
                .indexOf(
                    "light rice yogurt"
                )

        val secondCandidateIndex =
            providerRequest.userPrompt
                .indexOf(
                    "rice cakes yogurt coating"
                )

        assert(
            firstCandidateIndex <
                    secondCandidateIndex
        )
    }


    @Test
    fun doesNotTreatDiagnosticScoreAsMatchConfidence() {

        val providerRequest =
            DefaultCatalogKnowledgeMatchPromptFactory()
                .create(
                    request =
                        createRequest()
                )

        assertContains(
            providerRequest.systemPrompt,
            "diagnosticScore"
        )

        assertContains(
            providerRequest.systemPrompt,
            "sharedTokens"
        )

        assertContains(
            providerRequest.systemPrompt,
            "retrieval metadata only"
        )

        assertContains(
            providerRequest.systemPrompt,
            "do not prove that the foods are identical"
        )

        assertFalse(
            providerRequest.systemPrompt.contains(
                "Select the candidate with the highest diagnosticScore",
                ignoreCase = true
            )
        )
    }


    private fun createRequest():
            CatalogKnowledgeMatchRequest =
        CatalogKnowledgeMatchRequest(
            catalogKey =
                "rice yogurt",
            serverArtifact =
                "nutrition.json",
            candidates = listOf(
                CatalogKnowledgeMatchCandidate(
                    serverKey =
                        "light rice yogurt",
                    diagnosticScore =
                        0.82,
                    sharedTokens = listOf(
                        "rice",
                        "yogurt"
                    )
                ),
                CatalogKnowledgeMatchCandidate(
                    serverKey =
                        "rice cakes yogurt coating",
                    diagnosticScore =
                        0.61,
                    sharedTokens = listOf(
                        "rice",
                        "yogurt"
                    )
                )
            )
        )
}