package de.shopme.tools.knowledge.rebuild.nutrition.coverage

data class NutritionNoRequestGapDiagnosticEntry(
    val catalogKey: String,
    val reason: NutritionNoRequestGapReason,
    val catalogPresent: Boolean,
    val matchReportEntryPresent: Boolean,
    val matchReportEntryMatched: Boolean,
    val matchReportEntryUnmatched: Boolean,
    val nearestCandidateCount: Int?,
    val requestPresent: Boolean,
    val mappingPresent: Boolean,
    val details: String
) {

    init {
        require(
            catalogKey.isNotBlank()
        ) {
            "catalogKey must not be blank."
        }

        require(
            details.isNotBlank()
        ) {
            "details must not be blank."
        }

        require(
            !(matchReportEntryMatched &&
                    matchReportEntryUnmatched)
        ) {
            "A match-report entry cannot be matched and unmatched simultaneously."
        }

        require(
            !matchReportEntryMatched ||
                    matchReportEntryPresent
        ) {
            "A matched match-report entry must be present."
        }

        require(
            !matchReportEntryUnmatched ||
                    matchReportEntryPresent
        ) {
            "An unmatched match-report entry must be present."
        }

        require(
            nearestCandidateCount == null ||
                    nearestCandidateCount >= 0
        ) {
            "nearestCandidateCount must not be negative."
        }

        require(
            nearestCandidateCount == null ||
                    matchReportEntryPresent
        ) {
            "nearestCandidateCount requires a present match-report entry."
        }
    }
}