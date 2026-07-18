package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequests
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatcher
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogKnowledgeMatcherContractTest {

    @Test
    fun delegatesBatchMatchingToSingleRequestMatcher() {

        val matcher =
            object : CatalogKnowledgeMatcher {

                override fun match(
                    request: CatalogKnowledgeMatchRequest
                ) =
                    CatalogKnowledgeMatchDecision(
                        catalogKey =
                            request.catalogKey,
                        serverArtifact =
                            request.serverArtifact,
                        type =
                            CatalogKnowledgeMatchDecisionType.NO_MATCH,
                        selectedServerKey =
                            null,
                        confidence =
                            0.5,
                        reason =
                            "Fake matcher"
                    )
            }

        val requests =
            CatalogKnowledgeMatchRequests(
                version =
                    CatalogKnowledgeMatchRequestContract.CURRENT_VERSION,
                requests = listOf(
                    CatalogKnowledgeMatchRequest(
                        catalogKey =
                            "apple juice",
                        serverArtifact =
                            "nutrition.json",
                        candidates = listOf(
                            CatalogKnowledgeMatchCandidate(
                                serverKey =
                                    "apple juice",
                                diagnosticScore =
                                    1.0,
                                sharedTokens =
                                    listOf(
                                        "apple",
                                        "juice"
                                    )
                            )
                        )
                    )
                )
            )

        val result =
            matcher.match(
                requests
            )

        assertEquals(
            1,
            result.decisions.size
        )

        assertEquals(
            CatalogKnowledgeMatchDecisionType.NO_MATCH,
            result.decisions.single().type
        )
    }
}