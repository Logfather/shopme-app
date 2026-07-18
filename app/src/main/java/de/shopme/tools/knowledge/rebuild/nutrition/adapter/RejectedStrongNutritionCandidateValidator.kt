package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.mapping.catalog.representative.DeterministicRepresentativeNutritionMappingValidator
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingRequest

class RejectedStrongNutritionCandidateValidator(
    private val representativeValidator:
    DeterministicRepresentativeNutritionMappingValidator =
        DeterministicRepresentativeNutritionMappingValidator()
) {

    fun validate(
        candidates:
        Collection<RejectedStrongNutritionCandidateValidationRequest>
    ): RejectedStrongNutritionCandidateValidationReport {

        val normalizedCandidates =
            candidates
                .map {
                    it.normalized()
                }
                .sortedBy {
                    normalizeKey(
                        value =
                            it.catalogKey
                    )
                }

        val duplicateCatalogKeys =
            normalizedCandidates
                .groupingBy {
                    normalizeKey(
                        value =
                            it.catalogKey
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Duplicate rejected strong nutrition validation requests: " +
                    duplicateCatalogKeys
                        .sorted()
                        .joinToString()
        }

        val entries =
            normalizedCandidates
                .map { candidate ->

                    val decision =
                        representativeValidator.validate(
                            request =
                                RepresentativeNutritionMappingRequest(
                                    catalogKey =
                                        candidate.catalogKey,
                                    serverKey =
                                        candidate.selectedServerKey,
                                    confidence =
                                        candidate.originalConfidence,
                                    candidateRank =
                                        candidate.candidateRank,
                                    diagnosticScore =
                                        candidate.diagnosticScore,
                                    sharedTokens =
                                        candidate.sharedTokens
                                )
                        )

                    require(
                        normalizeKey(
                            value =
                                decision.catalogKey
                        ) ==
                                normalizeKey(
                                    value =
                                        candidate.catalogKey
                                )
                    ) {
                        "Representative validator returned a different " +
                                "catalogKey: expected=" +
                                "'${candidate.catalogKey}', actual=" +
                                "'${decision.catalogKey}'."
                    }

                    require(
                        normalizeKey(
                            value =
                                decision.serverKey
                        ) ==
                                normalizeKey(
                                    value =
                                        candidate.selectedServerKey
                                )
                    ) {
                        "Representative validator returned a different " +
                                "serverKey for '${candidate.catalogKey}': " +
                                "expected='${candidate.selectedServerKey}', " +
                                "actual='${decision.serverKey}'."
                    }

                    RejectedStrongNutritionCandidateValidationEntry(
                        catalogKey =
                            candidate.catalogKey,
                        selectedServerKey =
                            candidate.selectedServerKey,
                        diagnosticType =
                            candidate.diagnosticType,
                        originalNoMatchCause =
                            candidate.originalNoMatchCause,
                        originalConfidence =
                            candidate.originalConfidence,
                        candidateRank =
                            candidate.candidateRank,
                        diagnosticScore =
                            candidate.diagnosticScore,
                        sharedTokens =
                            candidate.sharedTokens,
                        decisionType =
                            decision.type,
                        accepted =
                            decision.accepted,
                        reasons =
                            decision.reasons,
                        details =
                            createDetails(
                                candidate =
                                    candidate,
                                accepted =
                                    decision.accepted,
                                decisionType =
                                    decision.type.name,
                                reasons =
                                    decision.reasons.map {
                                        it.name
                                    }
                            )
                    )
                }
                .sortedBy {
                    normalizeKey(
                        value =
                            it.catalogKey
                    )
                }

        return RejectedStrongNutritionCandidateValidationReport(
            version =
                RejectedStrongNutritionCandidateValidationReport
                    .CURRENT_VERSION,
            candidateCount =
                entries.size,
            acceptedCount =
                entries.count {
                    it.accepted
                },
            rejectedCount =
                entries.count {
                    !it.accepted
                },
            entries =
                entries
        )
    }

    private fun createDetails(
        candidate:
        RejectedStrongNutritionCandidateValidationRequest,
        accepted: Boolean,
        decisionType: String,
        reasons: List<String>
    ): String {

        val outcome =
            if (accepted) {
                "accepted"
            } else {
                "rejected"
            }

        return "Rejected strong nutrition candidate was " +
                "$outcome by the deterministic representative " +
                "validator: catalogKey='${candidate.catalogKey}', " +
                "selectedServerKey='${candidate.selectedServerKey}', " +
                "diagnosticType='${candidate.diagnosticType}', " +
                "decisionType='$decisionType', reasons=" +
                reasons
                    .sorted()
                    .joinToString(
                        prefix =
                            "[",
                        postfix =
                            "]"
                    ) +
                "."
    }

    private fun normalizeKey(
        value: String
    ): String =
        value
            .trim()
            .lowercase()
            .replace(
                Regex("\\s+"),
                " "
            )
}

data class RejectedStrongNutritionCandidateValidationRequest(
    val catalogKey: String,
    val selectedServerKey: String,
    val diagnosticType: String,
    val originalNoMatchCause: String,
    val originalConfidence: Double,
    val candidateRank: Int,
    val diagnosticScore: Double,
    val sharedTokens: List<String>
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank."
        }

        require(selectedServerKey.isNotBlank()) {
            "selectedServerKey must not be blank."
        }

        require(diagnosticType.isNotBlank()) {
            "diagnosticType must not be blank."
        }

        require(originalNoMatchCause.isNotBlank()) {
            "originalNoMatchCause must not be blank."
        }

        require(originalConfidence in 0.0..1.0) {
            "originalConfidence must be between 0.0 and 1.0."
        }

        require(candidateRank >= 1) {
            "candidateRank must be at least 1."
        }

        require(diagnosticScore in 0.0..1.0) {
            "diagnosticScore must be between 0.0 and 1.0."
        }
    }

    fun normalized():
            RejectedStrongNutritionCandidateValidationRequest =
        copy(
            catalogKey =
                normalizeKey(
                    value =
                        catalogKey
                ),
            selectedServerKey =
                normalizeKey(
                    value =
                        selectedServerKey
                ),
            diagnosticType =
                diagnosticType.trim(),
            originalNoMatchCause =
                originalNoMatchCause.trim(),
            sharedTokens =
                sharedTokens
                    .asSequence()
                    .map {
                        normalizeKey(
                            value =
                                it
                        )
                    }
                    .filter {
                        it.isNotBlank()
                    }
                    .distinct()
                    .sorted()
                    .toList()
        )

    private fun normalizeKey(
        value: String
    ): String =
        value
            .trim()
            .lowercase()
            .replace(
                "-",
                " "
            )
            .replace(
                "_",
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
}