package de.shopme.tools.knowledge.rebuild.nutrition.coverage

data class NutritionCoverageGap(
    val catalogKey: String,
    val type: NutritionCoverageGapType,
    val noMatchCause: NutritionNoMatchCause?,
    val requestExists: Boolean,
    val decisionExists: Boolean,
    val decisionType: String?,
    val decisionSource: String?,
    val selectedServerKey: String?,
    val decisionConfidence: Double?,
    val candidateCount: Int,
    val topCandidateKey: String?,
    val topCandidateScore: Double?,
    val secondCandidateScore: Double?,
    val topScoreDelta: Double?,
    val topCandidateSharedTokens: List<String>,
    val mappingExists: Boolean,
    val details: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank."
        }

        require(candidateCount >= 0) {
            "candidateCount must not be negative."
        }

        require(
            decisionConfidence == null ||
                    decisionConfidence in 0.0..1.0
        ) {
            "decisionConfidence must be null or between 0.0 and 1.0."
        }

        require(
            topCandidateScore == null ||
                    topCandidateScore in 0.0..1.0
        ) {
            "topCandidateScore must be null or between 0.0 and 1.0."
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

        require(
            topCandidateSharedTokens.none {
                it.isBlank()
            }
        ) {
            "topCandidateSharedTokens must not contain blank values."
        }

        require(
            topCandidateSharedTokens ==
                    topCandidateSharedTokens
                        .distinct()
                        .sorted()
        ) {
            "topCandidateSharedTokens must be unique and sorted."
        }

        require(
            type ==
                    NutritionCoverageGapType.NO_MATCH ||
                    noMatchCause == null
        ) {
            "Only NO_MATCH gaps may have a noMatchCause."
        }

        require(
            type !=
                    NutritionCoverageGapType.NO_MATCH ||
                    noMatchCause != null
        ) {
            "Every NO_MATCH gap must have a noMatchCause."
        }

        require(details.isNotBlank()) {
            "details must not be blank."
        }

        require(
            requestExists ||
                    candidateCount == 0
        ) {
            "A missing request must not contain candidates."
        }

        require(
            candidateCount > 0 ||
                    topCandidateKey == null
        ) {
            "A gap without candidates must not contain a top candidate."
        }

        require(
            candidateCount > 0 ||
                    topCandidateScore == null
        ) {
            "A gap without candidates must not contain a top score."
        }

        require(
            candidateCount >= 2 ||
                    secondCandidateScore == null
        ) {
            "A gap with fewer than two candidates must not contain " +
                    "a second candidate score."
        }

        require(
            candidateCount >= 2 ||
                    topScoreDelta == null
        ) {
            "A gap with fewer than two candidates must not contain " +
                    "a top score delta."
        }
    }
}