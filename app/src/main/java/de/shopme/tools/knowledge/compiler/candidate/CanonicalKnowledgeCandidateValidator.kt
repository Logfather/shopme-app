package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class CanonicalKnowledgeCandidateValidator {

    fun validate(
        candidate: CanonicalKnowledgeCandidate
    ): CanonicalKnowledgeCandidateValidationResult {

        val errors = mutableListOf<String>()

        if (candidate.canonicalId.isBlank()) {
            errors += "Candidate canonicalId must not be blank"
        }

        if (candidate.aliases.any { it.isBlank() }) {
            errors += "Candidate aliases must not contain blank values"
        }

        if (candidate.aliases.size != candidate.aliases.distinct().size) {
            errors += "Candidate aliases must not contain duplicates"
        }

        if (candidate.metadata.source.isBlank()) {
            errors += "Candidate metadata source must not be blank"
        }

        if (candidate.metadata.confidence < 0.0 || candidate.metadata.confidence > 1.0) {
            errors += "Candidate metadata confidence must be between 0.0 and 1.0"
        }

        return CanonicalKnowledgeCandidateValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}