package de.shopme.testing.system.tools.knowledge.report

data class CatalogServerKnowledgeMatchReport(
    val artifactName: String,
    val catalogKeyCount: Int,
    val serverKeyCount: Long,
    val exactMatches: List<String>,
    val unmatched: List<UnmatchedCatalogKnowledgeKey>
)

data class UnmatchedCatalogKnowledgeKey(
    val catalogKey: String,
    val nearestCandidates: List<NearestServerKnowledgeCandidate>
)

data class NearestServerKnowledgeCandidate(
    val serverKey: String,
    val score: Double,
    val sharedTokens: List<String>
)