package de.shopme.tools.knowledge.ki_candidates

class CanonicalKnowledgeCandidateValidator {

    fun validate(
        candidate: CanonicalKnowledgeCandidate
    ): List<String> {

        val errors = mutableListOf<String>()

        if (candidate.canonicalId.isBlank()) {
            errors += "canonicalId must not be blank"
        }

        candidate.aliases.forEach { alias ->
            if (alias.isBlank()) {
                errors += "aliases must not contain blank values"
            }
        }

        val duplicateDimensions = candidate.dimensions
            .groupBy { it.dimension }
            .filterValues { it.size > 1 }
            .keys

        duplicateDimensions.forEach { dimension ->
            errors += "duplicate dimension candidate: $dimension"
        }

        if (candidate.dimensions.isEmpty()) {
            errors += "dimensions must not be empty"
        }

        if (candidate.metadata.confidence !in 0.0..1.0) {
            errors += "confidence must be between 0.0 and 1.0"
        }

        if (candidate.metadata.source.isBlank()) {
            errors += "source must not be blank"
        }

        if (candidate.metadata.version?.isBlank() == true) {
            errors += "version must not be blank when present"
        }

        if (candidate.metadata.sourceId?.isBlank() == true) {
            errors += "sourceId must not be blank when present"
        }

        return errors
    }
}