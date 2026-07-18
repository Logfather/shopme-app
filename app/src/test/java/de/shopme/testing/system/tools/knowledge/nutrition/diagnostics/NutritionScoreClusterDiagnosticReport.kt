package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class NutritionScoreClusterDiagnosticReport(
    val version: Int,
    val coverageGapFile: String,
    val matchRequestFile: String,
    val matchDiagnosticFile: String,
    val scoreClusterCount: Int,
    val requestPresentCount: Int,
    val diagnosticPresentCount: Int,
    val averageTopScore: Double?,
    val averageSecondScore: Double?,
    val averageScoreDelta: Double?,
    val countsByScoreDeltaBucket:
    Map<NutritionScoreDeltaBucket, Int>,
    val countsBySelectedRank:
    Map<String, Int>,
    val countsByDecisionType:
    Map<String, Int>,
    val countsByDecisionSource:
    Map<String, Int>,
    val countsByValidationStatus:
    Map<String, Int>,
    val entries:
    List<NutritionScoreClusterDiagnosticEntry>,
)

data class NutritionScoreClusterDiagnosticEntry(
    val catalogKey: String,
    val candidateCount: Int,
    val topCandidateKey: String?,
    val topCandidateScore: Double?,
    val secondCandidateKey: String?,
    val secondCandidateScore: Double?,
    val topScoreDelta: Double?,
    val scoreDeltaBucket: NutritionScoreDeltaBucket,
    val topCandidateSharedTokens: List<String>,
    val selectedCandidateKey: String?,
    val selectedCandidateScore: Double?,
    val selectedRank: Int?,
    val decisionType: String?,
    val decisionConfidence: Double?,
    val decisionSource: String?,
    val validationStatus: String?,
    val decisionReason: String?,
    val validationReason: String?,
    val requestPresent: Boolean,
    val diagnosticPresent: Boolean,
    val candidates: List<NutritionScoreClusterCandidate>,
)

data class NutritionScoreClusterCandidate(
    val rank: Int,
    val serverKey: String,
    val score: Double?,
    val sharedTokens: List<String>,
    val selected: Boolean,
)

enum class NutritionScoreDeltaBucket {
    EXACT_TIE,
    VERY_CLOSE,
    CLOSE,
    MODERATE,
    CLEAR,
    UNKNOWN;

    companion object {

        fun classify(
            scoreDelta: Double?,
        ): NutritionScoreDeltaBucket =
            when {
                scoreDelta == null ->
                    UNKNOWN

                scoreDelta <= 0.01 ->
                    EXACT_TIE

                scoreDelta <= 0.03 ->
                    VERY_CLOSE

                scoreDelta <= 0.07 ->
                    CLOSE

                scoreDelta <= 0.15 ->
                    MODERATE

                else ->
                    CLEAR
            }
    }
}