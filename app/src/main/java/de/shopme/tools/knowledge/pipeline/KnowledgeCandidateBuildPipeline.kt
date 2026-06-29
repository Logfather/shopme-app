package de.shopme.tools.knowledge.pipeline

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidateValidator
import de.shopme.tools.knowledge.source.KnowledgeSourceAdapter

class KnowledgeCandidateBuildPipeline(
    private val sourceAdapters: List<KnowledgeSourceAdapter>,
    private val validator: CanonicalKnowledgeCandidateValidator
) {

    fun build(): KnowledgeCandidateBuildResult {

        val validCandidates = mutableListOf<CanonicalKnowledgeCandidate>()
        val rejectedCandidates = mutableListOf<RejectedKnowledgeCandidate>()

        sourceAdapters
            .flatMap { it.load() }
            .forEach { candidate ->

                val errors = validator.validate(candidate)

                if (errors.isEmpty()) {
                    validCandidates += candidate
                } else {
                    rejectedCandidates += RejectedKnowledgeCandidate(
                        candidate = candidate,
                        validationErrors = errors
                    )
                }
            }

        return KnowledgeCandidateBuildResult(
            validCandidates = validCandidates,
            rejectedCandidates = rejectedCandidates
        )

    }

}