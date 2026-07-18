package de.shopme.tools.knowledge.ki_candidates

data class KnowledgeCandidateMergeResult(
    val candidates: List<CanonicalKnowledgeCandidate>,
    val conflicts: List<KnowledgeCandidateMergeConflict>
)