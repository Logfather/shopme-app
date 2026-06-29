package de.shopme.tools.knowledge.pipeline

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class KnowledgeCandidateBuildResult(

    val validCandidates: List<CanonicalKnowledgeCandidate>,

    val rejectedCandidates: List<RejectedKnowledgeCandidate>

) {

    val summary: KnowledgeCandidateBuildSummary
        get() = KnowledgeCandidateBuildSummary(
            loadedCandidates = validCandidates.size + rejectedCandidates.size,
            validCandidates = validCandidates.size,
            rejectedCandidates = rejectedCandidates.size
        )

}