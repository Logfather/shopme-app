package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingReason

data class RejectedStrongNutritionCandidateValidationEntry(
    val catalogKey: String,
    val selectedServerKey: String,
    val diagnosticType: String,
    val originalNoMatchCause: String,
    val originalConfidence: Double,
    val candidateRank: Int,
    val diagnosticScore: Double,
    val sharedTokens: List<String>,
    val decisionType:
    RepresentativeNutritionMappingDecisionType,
    val accepted: Boolean,
    val reasons:
    List<RepresentativeNutritionMappingReason>,
    val details: String
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

        require(
            sharedTokens.none {
                it.isBlank()
            }
        ) {
            "sharedTokens must not contain blank values."
        }

        require(
            sharedTokens ==
                    sharedTokens
                        .distinct()
                        .sorted()
        ) {
            "sharedTokens must be unique and sorted."
        }

        require(reasons.isNotEmpty()) {
            "reasons must not be empty."
        }

        require(
            reasons ==
                    reasons
                        .distinct()
                        .sortedBy {
                            it.ordinal
                        }
        ) {
            "reasons must be unique and sorted deterministically."
        }

        require(
            accepted ==
                    (
                            decisionType !=
                                    RepresentativeNutritionMappingDecisionType
                                        .INCOMPATIBLE
                            )
        ) {
            "accepted must correspond to decisionType."
        }

        require(details.isNotBlank()) {
            "details must not be blank."
        }
    }
}