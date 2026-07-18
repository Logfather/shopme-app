package de.shopme.testing.system.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMappingIdentity
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMappingValidationStatus
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionValidator
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisions
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequests
import de.shopme.tools.knowledge.mapping.catalog.CatalogServerKnowledgeMappingMethod
import kotlin.test.Test
import kotlin.test.assertEquals

class CatalogKnowledgeMatchDecisionValidatorTest {

    @Test
    fun validatesNutritionDecisionsAsReusableMappings() {

        val requests =
            CatalogKnowledgeMatchRequests(
                version =
                    CatalogKnowledgeMatchRequestContract.CURRENT_VERSION,
                requests =
                    listOf(
                        request(
                            catalogKey =
                                "low confidence yogurt",
                            candidate =
                                "light rice yogurt"
                        ),
                        request(
                            catalogKey =
                                "matjes herring in cream sauce",
                            candidate =
                                "herring fillets in cream sauce"
                        ),
                        request(
                            catalogKey =
                                "plain acerola juice",
                            candidate =
                                "mixed acerola fruit juice"
                        ),
                        request(
                            catalogKey =
                                "unknown selected key",
                            candidate =
                                "known candidate"
                        )
                    ).sortedWith(
                        CatalogKnowledgeMatchRequests.REQUEST_ORDER
                    )
            )

        val decisions =
            CatalogKnowledgeMatchDecisions(
                version =
                    CatalogKnowledgeMatchDecisionContract.CURRENT_VERSION,
                decisions =
                    listOf(
                        decision(
                            catalogKey =
                                "low confidence yogurt",
                            type =
                                CatalogKnowledgeMatchDecisionType.MATCH,
                            selectedServerKey =
                                "light rice yogurt",
                            confidence =
                                0.78
                        ),
                        decision(
                            catalogKey =
                                "matjes herring in cream sauce",
                            type =
                                CatalogKnowledgeMatchDecisionType.MATCH,
                            selectedServerKey =
                                "herring fillets in cream sauce",
                            confidence =
                                0.93
                        ),
                        decision(
                            catalogKey =
                                "plain acerola juice",
                            type =
                                CatalogKnowledgeMatchDecisionType.NO_MATCH,
                            selectedServerKey =
                                null,
                            confidence =
                                0.96
                        ),
                        decision(
                            catalogKey =
                                "unknown selected key",
                            type =
                                CatalogKnowledgeMatchDecisionType.MATCH,
                            selectedServerKey =
                                "invented server key",
                            confidence =
                                0.99
                        )
                    ).sortedWith(
                        CatalogKnowledgeMatchDecisions.DECISION_ORDER
                    )
            )

        val result =
            CatalogKnowledgeMatchDecisionValidator(
                minimumConfidence =
                    0.80
            ).validate(
                requests =
                    requests,
                decisions =
                    decisions,
                serverKeysByArtifact =
                    mapOf(
                        "nutrition.json" to
                                setOf(
                                    "herring fillets in cream sauce",
                                    "light rice yogurt",
                                    "mixed acerola fruit juice",
                                    "known candidate"
                                )
                    ),
                existingExactMappings =
                    emptySet()
            )

        assertEquals(
            1,
            result.mappings.mappings.size
        )

        val mapping =
            result.mappings.mappings.single()

        assertEquals(
            "matjes herring in cream sauce",
            mapping.catalogKey
        )

        assertEquals(
            "herring fillets in cream sauce",
            mapping.serverKey
        )

        assertEquals(
            CatalogServerKnowledgeMappingMethod.AI_VALIDATED,
            mapping.method
        )

        val statuses =
            result.report.validations
                .associate {
                    it.catalogKey to
                            it.status
                }

        assertEquals(
            CatalogKnowledgeMappingValidationStatus
                .REJECTED_LOW_CONFIDENCE,
            statuses[
                "low confidence yogurt"
            ]
        )

        assertEquals(
            CatalogKnowledgeMappingValidationStatus.ACCEPTED,
            statuses[
                "matjes herring in cream sauce"
            ]
        )

        assertEquals(
            CatalogKnowledgeMappingValidationStatus
                .REJECTED_NO_MATCH,
            statuses[
                "plain acerola juice"
            ]
        )

        assertEquals(
            CatalogKnowledgeMappingValidationStatus
                .REJECTED_SELECTED_KEY_NOT_CANDIDATE,
            statuses[
                "unknown selected key"
            ]
        )
    }


    @Test
    fun doesNotOverwriteExistingExactMapping() {

        val request =
            request(
                catalogKey =
                    "apple yogurt",
                candidate =
                    "apple yogurt light"
            )

        val decision =
            decision(
                catalogKey =
                    "apple yogurt",
                type =
                    CatalogKnowledgeMatchDecisionType.MATCH,
                selectedServerKey =
                    "apple yogurt light",
                confidence =
                    0.95
            )

        val result =
            CatalogKnowledgeMatchDecisionValidator(
                minimumConfidence =
                    0.80
            ).validate(
                requests =
                    CatalogKnowledgeMatchRequests(
                        version =
                            CatalogKnowledgeMatchRequestContract
                                .CURRENT_VERSION,
                        requests =
                            listOf(request)
                    ),
                decisions =
                    CatalogKnowledgeMatchDecisions(
                        version =
                            CatalogKnowledgeMatchDecisionContract
                                .CURRENT_VERSION,
                        decisions =
                            listOf(decision)
                    ),
                serverKeysByArtifact =
                    mapOf(
                        "nutrition.json" to
                                setOf(
                                    "apple yogurt light"
                                )
                    ),
                existingExactMappings =
                    setOf(
                        CatalogKnowledgeMappingIdentity(
                            catalogKey =
                                "apple yogurt",
                            serverArtifact =
                                "nutrition.json"
                        )
                    )
            )

        assertEquals(
            0,
            result.mappings.mappings.size
        )

        assertEquals(
            CatalogKnowledgeMappingValidationStatus
                .REJECTED_EXISTING_EXACT_MAPPING,
            result.report.validations
                .single()
                .status
        )
    }


    private fun request(
        catalogKey: String,
        candidate: String
    ): CatalogKnowledgeMatchRequest =
        CatalogKnowledgeMatchRequest(
            catalogKey =
                catalogKey,
            serverArtifact =
                "nutrition.json",
            candidates =
                listOf(
                    CatalogKnowledgeMatchCandidate(
                        serverKey =
                            candidate,
                        diagnosticScore =
                            0.90,
                        sharedTokens =
                            catalogKey
                                .split(" ")
                                .distinct()
                                .sorted()
                    )
                )
        )


    private fun decision(
        catalogKey: String,
        type: CatalogKnowledgeMatchDecisionType,
        selectedServerKey: String?,
        confidence: Double
    ): CatalogKnowledgeMatchDecision =
        CatalogKnowledgeMatchDecision(
            catalogKey =
                catalogKey,
            serverArtifact =
                "nutrition.json",
            type =
                type,
            selectedServerKey =
                selectedServerKey,
            confidence =
                confidence,
            reason =
                "Recorded validator test decision"
        )
}