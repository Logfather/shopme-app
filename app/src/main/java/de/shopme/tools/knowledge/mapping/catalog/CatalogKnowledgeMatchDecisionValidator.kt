package de.shopme.tools.knowledge.mapping.catalog

class CatalogKnowledgeMatchDecisionValidator(
    private val minimumConfidence: Double
) {

    init {
        require(minimumConfidence in 0.0..1.0) {
            "minimumConfidence must be between 0.0 and 1.0"
        }
    }


    fun validate(
        requests: CatalogKnowledgeMatchRequests,
        decisions: CatalogKnowledgeMatchDecisions,
        serverKeysByArtifact: Map<String, Set<String>>,
        existingExactMappings: Set<CatalogKnowledgeMappingIdentity>
    ): CatalogKnowledgeMatchDecisionValidationResult {

        val requestsByIdentity =
            requests.requests
                .associateBy {
                    CatalogKnowledgeMappingIdentity(
                        catalogKey =
                            it.catalogKey,
                        serverArtifact =
                            it.serverArtifact
                    )
                }

        val validations =
            decisions.decisions
                .map { decision ->

                    validateDecision(
                        decision = decision,
                        request =
                            requestsByIdentity[
                                CatalogKnowledgeMappingIdentity(
                                    catalogKey =
                                        decision.catalogKey,
                                    serverArtifact =
                                        decision.serverArtifact
                                )
                            ],
                        serverKeys =
                            serverKeysByArtifact[
                                decision.serverArtifact
                            ].orEmpty(),
                        existingExactMappings =
                            existingExactMappings
                    )
                }
                .sortedWith(
                    CatalogKnowledgeMappingValidationReport
                        .VALIDATION_ORDER
                )

        val mappings =
            validations
                .filter {
                    it.isAccepted
                }
                .map { validation ->

                    CatalogServerKnowledgeMapping(
                        catalogKey =
                            validation.catalogKey,
                        serverKey =
                            requireNotNull(
                                validation.selectedServerKey
                            ),
                        sourceArtifact =
                            validation.serverArtifact,
                        method =
                            CatalogServerKnowledgeMappingMethod
                                .AI_VALIDATED,
                        confidence =
                            validation.confidence,
                        reason =
                            validation.reason
                    )
                }
                .sortedWith(
                    CatalogServerKnowledgeMappings.MAPPING_ORDER
                )

        return CatalogKnowledgeMatchDecisionValidationResult(
            mappings =
                CatalogServerKnowledgeMappings(
                    version =
                        CatalogServerKnowledgeMappings.CURRENT_VERSION,
                    mappings =
                        mappings
                ),
            report =
                CatalogKnowledgeMappingValidationReport(
                    minimumConfidence =
                        minimumConfidence,
                    validations =
                        validations
                )
        )
    }


    private fun validateDecision(
        decision: CatalogKnowledgeMatchDecision,
        request: CatalogKnowledgeMatchRequest?,
        serverKeys: Set<String>,
        existingExactMappings:
        Set<CatalogKnowledgeMappingIdentity>
    ): CatalogKnowledgeMappingValidation {

        if (request == null) {
            return decision.rejected(
                status =
                    CatalogKnowledgeMappingValidationStatus
                        .REJECTED_MISSING_REQUEST,
                reason =
                    "No matching request exists for decision"
            )
        }

        if (
            decision.type ==
            CatalogKnowledgeMatchDecisionType.NO_MATCH
        ) {
            return decision.rejected(
                status =
                    CatalogKnowledgeMappingValidationStatus
                        .REJECTED_NO_MATCH,
                reason =
                    decision.reason
            )
        }

        if (
            decision.confidence <
            minimumConfidence
        ) {
            return decision.rejected(
                status =
                    CatalogKnowledgeMappingValidationStatus
                        .REJECTED_LOW_CONFIDENCE,
                reason =
                    "Decision confidence ${decision.confidence} " +
                            "is below minimum $minimumConfidence"
            )
        }

        val selectedServerKey =
            requireNotNull(
                decision.selectedServerKey
            )

        if (
            request.candidates.none {
                it.serverKey ==
                        selectedServerKey
            }
        ) {
            return decision.rejected(
                status =
                    CatalogKnowledgeMappingValidationStatus
                        .REJECTED_SELECTED_KEY_NOT_CANDIDATE,
                reason =
                    "Selected server key was not part of request candidates"
            )
        }

        if (
            selectedServerKey !in
            serverKeys
        ) {
            return decision.rejected(
                status =
                    CatalogKnowledgeMappingValidationStatus
                        .REJECTED_SELECTED_KEY_NOT_IN_SERVER_ARTIFACT,
                reason =
                    "Selected server key does not exist in " +
                            decision.serverArtifact
            )
        }

        val identity =
            CatalogKnowledgeMappingIdentity(
                catalogKey =
                    decision.catalogKey,
                serverArtifact =
                    decision.serverArtifact
            )

        if (
            identity in
            existingExactMappings
        ) {
            return decision.rejected(
                status =
                    CatalogKnowledgeMappingValidationStatus
                        .REJECTED_EXISTING_EXACT_MAPPING,
                reason =
                    "Existing EXACT mapping must not be overwritten"
            )
        }

        return CatalogKnowledgeMappingValidation(
            catalogKey =
                decision.catalogKey,
            serverArtifact =
                decision.serverArtifact,
            selectedServerKey =
                selectedServerKey,
            confidence =
                decision.confidence,
            status =
                CatalogKnowledgeMappingValidationStatus.ACCEPTED,
            reason =
                decision.reason
        )
    }


    private fun CatalogKnowledgeMatchDecision.rejected(
        status: CatalogKnowledgeMappingValidationStatus,
        reason: String
    ): CatalogKnowledgeMappingValidation =
        CatalogKnowledgeMappingValidation(
            catalogKey =
                catalogKey,
            serverArtifact =
                serverArtifact,
            selectedServerKey =
                selectedServerKey,
            confidence =
                confidence,
            status =
                status,
            reason =
                reason
        )
}