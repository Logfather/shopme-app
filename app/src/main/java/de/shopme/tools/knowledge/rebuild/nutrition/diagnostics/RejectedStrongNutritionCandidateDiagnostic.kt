package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

data class RejectedStrongNutritionCandidateDiagnostic(
    val catalogKey: String,
    val noMatchCause: String,
    val topCandidateKey: String,
    val topCandidateScore: Double,
    val topCandidateRank: Int,
    val topCandidateSharedTokens: List<String>,
    val secondCandidateScore: Double?,
    val topScoreDelta: Double?,
    val decisionConfidence: Double,
    val decisionSource: String,
    val decisionReason: String,
    val catalogTokens: List<String>,
    val candidateTokens: List<String>,
    val sharedCoreTokens: List<String>,
    val missingCatalogCoreTokens: List<String>,
    val additionalCandidateCoreTokens: List<String>,
    val specializationRiskTypes:
    List<NutritionSpecializationRiskType>,
    val highRiskAdditionalTokens: List<String>,
    val unknownAdditionalTokens: List<String>,
    val catalogProductClasses: List<String>,
    val candidateProductClasses: List<String>,
    val modifierConflicts: List<String>,
    val processingStateConflicts: List<String>,
    val productFormConflicts: List<String>,
    val diagnosticType:
    RejectedStrongNutritionCandidateDiagnosticType,
    val representativeReviewRecommended: Boolean,
    val details: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank."
        }

        require(
            noMatchCause in
                    SUPPORTED_NO_MATCH_CAUSES
        ) {
            "Unsupported noMatchCause: $noMatchCause."
        }

        require(topCandidateKey.isNotBlank()) {
            "topCandidateKey must not be blank."
        }

        require(topCandidateScore in 0.0..1.0) {
            "topCandidateScore must be between 0.0 and 1.0."
        }

        require(topCandidateRank > 0) {
            "topCandidateRank must be greater than zero."
        }

        require(
            secondCandidateScore == null ||
                    secondCandidateScore in 0.0..1.0
        ) {
            "secondCandidateScore must be null or between 0.0 and 1.0."
        }

        require(
            topScoreDelta == null ||
                    topScoreDelta >= 0.0
        ) {
            "topScoreDelta must be null or non-negative."
        }

        require(decisionConfidence in 0.0..1.0) {
            "decisionConfidence must be between 0.0 and 1.0."
        }

        require(decisionSource.isNotBlank()) {
            "decisionSource must not be blank."
        }

        require(decisionReason.isNotBlank()) {
            "decisionReason must not be blank."
        }

        require(details.isNotBlank()) {
            "details must not be blank."
        }

        requireSortedUnique(
            values =
                topCandidateSharedTokens,
            fieldName =
                "topCandidateSharedTokens"
        )

        requireSortedUnique(
            values =
                catalogTokens,
            fieldName =
                "catalogTokens"
        )

        requireSortedUnique(
            values =
                candidateTokens,
            fieldName =
                "candidateTokens"
        )

        requireSortedUnique(
            values =
                sharedCoreTokens,
            fieldName =
                "sharedCoreTokens"
        )

        requireSortedUnique(
            values =
                missingCatalogCoreTokens,
            fieldName =
                "missingCatalogCoreTokens"
        )

        requireSortedUnique(
            values =
                additionalCandidateCoreTokens,
            fieldName =
                "additionalCandidateCoreTokens"
        )

        requireSortedUnique(
            values =
                catalogProductClasses,
            fieldName =
                "catalogProductClasses"
        )

        requireSortedUnique(
            values =
                candidateProductClasses,
            fieldName =
                "candidateProductClasses"
        )

        requireSortedUnique(
            values =
                modifierConflicts,
            fieldName =
                "modifierConflicts"
        )

        requireSortedUnique(
            values =
                processingStateConflicts,
            fieldName =
                "processingStateConflicts"
        )

        requireSortedUnique(
            values =
                productFormConflicts,
            fieldName =
                "productFormConflicts"
        )

        require(
            !representativeReviewRecommended ||
                    diagnosticType in
                    REVIEW_RECOMMENDED_TYPES
        ) {
            "representativeReviewRecommended may only be true for " +
                    "review-compatible diagnostic types."
        }

        require(
            specializationRiskTypes ==
                    specializationRiskTypes
                        .distinct()
                        .sortedBy {
                            it.name
                        }
        ) {
            "specializationRiskTypes must be unique and sorted."
        }

        requireSortedUnique(
            values =
                highRiskAdditionalTokens,
            fieldName =
                "highRiskAdditionalTokens"
        )

        requireSortedUnique(
            values =
                unknownAdditionalTokens,
            fieldName =
                "unknownAdditionalTokens"
        )

        require(
            highRiskAdditionalTokens.all {
                it in
                        additionalCandidateCoreTokens
            }
        ) {
            "highRiskAdditionalTokens must be contained in " +
                    "additionalCandidateCoreTokens."
        }

        require(
            unknownAdditionalTokens.all {
                it in
                        additionalCandidateCoreTokens
            }
        ) {
            "unknownAdditionalTokens must be contained in " +
                    "additionalCandidateCoreTokens."
        }
    }

    private fun requireSortedUnique(
        values: List<String>,
        fieldName: String
    ) {
        require(
            values.none {
                it.isBlank()
            }
        ) {
            "$fieldName must not contain blank values."
        }

        require(
            values ==
                    values
                        .distinct()
                        .sorted()
        ) {
            "$fieldName must be unique and sorted."
        }
    }

    companion object {

        val SUPPORTED_NO_MATCH_CAUSES =
            setOf(
                "STRONG_TOP_CANDIDATE_REJECTED",
                "MODERATE_TOP_CANDIDATE_REJECTED"
            )

        val REVIEW_RECOMMENDED_TYPES =
            setOf(
                RejectedStrongNutritionCandidateDiagnosticType
                    .LIKELY_REPRESENTATIVE,
                RejectedStrongNutritionCandidateDiagnosticType
                    .COMPATIBLE_SPECIALIZATION,
                RejectedStrongNutritionCandidateDiagnosticType
                    .COMPATIBLE_GENERALIZATION,
                RejectedStrongNutritionCandidateDiagnosticType
                    .ADDITIONAL_NON_CRITICAL_MODIFIER,
                RejectedStrongNutritionCandidateDiagnosticType
                    .MISSING_NON_CRITICAL_MODIFIER
            )
    }
}