package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class NutritionScoreClusterCandidateAnalysis(
    val version: Int,
    val sourceScoreClusterCount: Int,
    val entryCount: Int,
    val candidateCount: Int,
    val countsByCandidateCount: Map<Int, Int>,
    val countsBySharedTokenCount: Map<Int, Int>,
    val countsByExactTokenSetMatch: Map<Boolean, Int>,
    val countsByContainmentType:
    Map<NutritionScoreClusterContainmentType, Int>,
    val entries:
    List<NutritionScoreClusterCandidateAnalysisEntry>,
)

data class NutritionScoreClusterCandidateAnalysisEntry(
    val catalogKey: String,
    val catalogTokens: List<String>,
    val candidateCount: Int,
    val topCandidateScore: Double?,
    val secondCandidateScore: Double?,
    val scoreDelta: Double?,
    val candidates:
    List<NutritionScoreClusterCandidateAnalysisCandidate>,
)

data class NutritionScoreClusterCandidateAnalysisCandidate(
    val rank: Int,
    val serverKey: String,
    val score: Double?,
    val catalogTokens: List<String>,
    val serverTokens: List<String>,
    val sharedTokens: List<String>,
    val sharedTokenCount: Int,
    val catalogTokenCount: Int,
    val serverTokenCount: Int,
    val exactTokenSetMatch: Boolean,
    val containmentType:
    NutritionScoreClusterContainmentType,
    val catalogCoverage: Double,
    val serverCoverage: Double,
    val selected: Boolean,
)

enum class NutritionScoreClusterContainmentType {
    IDENTICAL,
    CATALOG_CONTAINS_SERVER,
    SERVER_CONTAINS_CATALOG,
    PARTIAL,
    NONE,
}