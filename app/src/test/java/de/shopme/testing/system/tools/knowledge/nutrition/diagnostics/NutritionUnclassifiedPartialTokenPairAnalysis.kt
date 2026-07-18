package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

data class NutritionUnclassifiedPartialTokenPairAnalysis(
    val version: Int,
    val sourcePartialCandidateCount: Int,
    val sourceUnclassifiedPartialCount: Int,
    val analyzedRelationshipCount: Int,
    val singleTokenPairRelationshipCount: Int,
    val multiTokenRelationshipCount: Int,
    val tokenPairObservationCount: Int,
    val countsByCatalogOnlyToken: Map<String, Int>,
    val countsByServerOnlyToken: Map<String, Int>,
    val countsByTokenPair: Map<String, Int>,
    val countsByNormalizedTokenPair: Map<String, Int>,
    val countsByCatalogOnlyTokenCount: Map<Int, Int>,
    val countsByServerOnlyTokenCount: Map<Int, Int>,
    val topCatalogOnlyTokens:
    List<NutritionUnclassifiedPartialTokenCount>,
    val topServerOnlyTokens:
    List<NutritionUnclassifiedPartialTokenCount>,
    val topTokenPairs:
    List<NutritionUnclassifiedPartialTokenPairCount>,
    val topNormalizedTokenPairs:
    List<NutritionUnclassifiedPartialTokenPairCount>,
    val entries:
    List<NutritionUnclassifiedPartialTokenPairEntry>,
)

data class NutritionUnclassifiedPartialTokenCount(
    val token: String,
    val count: Int,
)

data class NutritionUnclassifiedPartialTokenPairCount(
    val catalogToken: String,
    val serverToken: String,
    val pairKey: String,
    val count: Int,
)

data class NutritionUnclassifiedPartialTokenPairEntry(
    val catalogKey: String,
    val serverKey: String,
    val rank: Int,
    val sharedTokens: List<String>,
    val catalogOnlyTokens: List<String>,
    val serverOnlyTokens: List<String>,
    val singleTokenPair: Boolean,
    val tokenPairs:
    List<NutritionUnclassifiedPartialTokenPairObservation>,
)

data class NutritionUnclassifiedPartialTokenPairObservation(
    val catalogToken: String,
    val serverToken: String,
    val pairKey: String,
    val normalizedCatalogToken: String,
    val normalizedServerToken: String,
    val normalizedPairKey: String,
)