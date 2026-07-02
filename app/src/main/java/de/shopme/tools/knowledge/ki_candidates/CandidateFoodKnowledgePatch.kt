package de.shopme.tools.knowledge.ki_candidates

data class CandidateFoodKnowledgePatch(
    val canonicalId: String,
    val aliases: Set<String>,
    val dimensions: List<KnowledgeDimensionCandidate>,
    val metadata: CandidateMetadata
)