package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

data class RejectedStrongNutritionCandidateDiagnosticReport(
    val version: Int,
    val selectedGapCount: Int,
    val strongTopCandidateCount: Int,
    val moderateTopCandidateCount: Int,
    val representativeReviewRecommendedCount: Int,
    val conflictCount: Int,
    val countsByDiagnosticType: Map<String, Int>,
    val diagnostics:
    List<RejectedStrongNutritionCandidateDiagnostic>
) {

    init {
        require(version > 0) {
            "version must be greater than zero."
        }

        require(selectedGapCount >= 0) {
            "selectedGapCount must not be negative."
        }

        require(strongTopCandidateCount >= 0) {
            "strongTopCandidateCount must not be negative."
        }

        require(moderateTopCandidateCount >= 0) {
            "moderateTopCandidateCount must not be negative."
        }

        require(representativeReviewRecommendedCount >= 0) {
            "representativeReviewRecommendedCount must not be negative."
        }

        require(conflictCount >= 0) {
            "conflictCount must not be negative."
        }

        require(
            selectedGapCount ==
                    diagnostics.size
        ) {
            "selectedGapCount must equal diagnostics size."
        }

        require(
            strongTopCandidateCount +
                    moderateTopCandidateCount ==
                    selectedGapCount
        ) {
            "Strong and moderate candidate counts must cover every " +
                    "selected gap."
        }

        require(
            representativeReviewRecommendedCount ==
                    diagnostics.count {
                        it.representativeReviewRecommended
                    }
        ) {
            "representativeReviewRecommendedCount differs from " +
                    "diagnostics."
        }

        require(
            conflictCount ==
                    diagnostics.count {
                        it.diagnosticType in
                                CONFLICT_TYPES
                    }
        ) {
            "conflictCount differs from diagnostics."
        }

        require(
            diagnostics ==
                    diagnostics.sortedBy {
                        it.catalogKey
                    }
        ) {
            "Diagnostics must be sorted by catalogKey."
        }

        val duplicateKeys =
            diagnostics
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateKeys.isEmpty()) {
            "Diagnostics contain duplicate catalog keys: " +
                    duplicateKeys
                        .sorted()
                        .joinToString()
        }

        val actualCounts =
            diagnostics
                .groupingBy {
                    it.diagnosticType.name
                }
                .eachCount()
                .toSortedMap()

        require(
            countsByDiagnosticType ==
                    actualCounts
        ) {
            "countsByDiagnosticType differs from diagnostics."
        }
    }

    companion object {

        const val CURRENT_VERSION =
            1

        val CONFLICT_TYPES =
            setOf(
                RejectedStrongNutritionCandidateDiagnosticType
                    .CRITICAL_MODIFIER_CONFLICT,
                RejectedStrongNutritionCandidateDiagnosticType
                    .PROCESSING_STATE_CONFLICT,
                RejectedStrongNutritionCandidateDiagnosticType
                    .PRODUCT_FORM_CONFLICT,
                RejectedStrongNutritionCandidateDiagnosticType
                    .DIFFERENT_PRODUCT_CLASS,
                RejectedStrongNutritionCandidateDiagnosticType
                    .BRAND_OR_VARIANT_MISMATCH,
                RejectedStrongNutritionCandidateDiagnosticType
                    .INSUFFICIENT_SEMANTIC_EVIDENCE
            )
    }
}