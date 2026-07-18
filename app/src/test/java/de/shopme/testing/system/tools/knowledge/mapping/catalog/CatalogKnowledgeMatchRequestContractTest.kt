package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequests
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CatalogKnowledgeMatchRequestContractTest {

    @Test
    fun createsVersionedMatchRequestBatch() {

        val request =
            CatalogKnowledgeMatchRequest(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                candidates = listOf(
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed pasteurized",
                        diagnosticScore = 0.81,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    ),
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed uht",
                        diagnosticScore = 0.81,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    )
                )
            )

        val batch =
            CatalogKnowledgeMatchRequests(
                version =
                    CatalogKnowledgeMatchRequestContract.CURRENT_VERSION,
                requests = listOf(request)
            )

        assertEquals(
            1,
            batch.version
        )

        assertEquals(
            request,
            batch.requests.single()
        )
    }


    @Test
    fun rejectsEmptyCandidateList() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchRequest(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                candidates =
                    emptyList()
            )
        }
    }


    @Test
    fun rejectsDuplicateCandidates() {

        val candidate =
            CatalogKnowledgeMatchCandidate(
                serverKey =
                    "milk semi skimmed uht",
                diagnosticScore = 0.81,
                sharedTokens = listOf(
                    "milk",
                    "semi",
                    "skimmed"
                )
            )

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchRequest(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                candidates = listOf(
                    candidate,
                    candidate
                )
            )
        }
    }


    @Test
    fun rejectsNonDeterministicCandidateOrder() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchRequest(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                candidates = listOf(
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed uht",
                        diagnosticScore = 0.70,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    ),
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed pasteurized",
                        diagnosticScore = 0.90,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    )
                )
            )
        }
    }


    @Test
    fun acceptsAlphabeticOrderForEqualScores() {

        val request =
            CatalogKnowledgeMatchRequest(
                catalogKey =
                    "semi skimmed milk",
                serverArtifact =
                    "environmental_impact.json",
                candidates = listOf(
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed pasteurized",
                        diagnosticScore = 0.81,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    ),
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            "milk semi skimmed uht",
                        diagnosticScore = 0.81,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    )
                )
            )

        assertEquals(
            listOf(
                "milk semi skimmed pasteurized",
                "milk semi skimmed uht"
            ),
            request.candidates.map {
                it.serverKey
            }
        )
    }


    @Test
    fun rejectsDuplicateRequestForCatalogKeyAndArtifact() {

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
                        diagnosticScore = 0.81,
                        sharedTokens = listOf(
                            "milk",
                            "semi",
                            "skimmed"
                        )
                    )
                )
            )

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchRequests(
                version =
                    CatalogKnowledgeMatchRequestContract.CURRENT_VERSION,
                requests = listOf(
                    request,
                    request
                )
            )
        }
    }


    @Test
    fun rejectsInvalidDiagnosticScore() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchCandidate(
                serverKey =
                    "milk semi skimmed uht",
                diagnosticScore = 1.01,
                sharedTokens = listOf(
                    "milk"
                )
            )
        }
    }


    @Test
    fun rejectsDuplicateSharedTokens() {

        assertFailsWith<IllegalArgumentException> {

            CatalogKnowledgeMatchCandidate(
                serverKey =
                    "milk semi skimmed uht",
                diagnosticScore = 0.81,
                sharedTokens = listOf(
                    "milk",
                    "milk"
                )
            )
        }
    }
}