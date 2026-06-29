package de.shopme.tools.knowledge.compiler.candidate

class KnowledgeImportBatchValidator(
    private val candidateValidator: CanonicalKnowledgeCandidateValidator =
        CanonicalKnowledgeCandidateValidator()
) {

    fun validate(
        batch: KnowledgeImportBatch
    ): KnowledgeImportBatchValidationResult {

        val errors = mutableListOf<String>()

        if (batch.metadata.source.isBlank()) {
            errors += "Import batch source must not be blank"
        }

        if (batch.metadata.generatedBy.isBlank()) {
            errors += "Import batch generatedBy must not be blank"
        }

        if (batch.metadata.generatedAt.isBlank()) {
            errors += "Import batch generatedAt must not be blank"
        }

        if (batch.candidates.isEmpty()) {
            errors += "Import batch candidates must not be empty"
        }

        batch.candidates.forEach { candidate ->

            val result =
                candidateValidator.validate(candidate)

            result.errors.forEach { error ->
                errors += "Candidate ${candidate.canonicalId}: $error"
            }
        }

        val duplicatedCanonicalIds =
            batch.candidates
                .map { it.canonicalId }
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .keys

        duplicatedCanonicalIds.forEach { canonicalId ->
            errors += "Duplicate candidate canonicalId: $canonicalId"
        }

        val duplicatedAliases =
            batch.candidates
                .flatMap { it.aliases }
                .groupingBy { it }
                .eachCount()
                .filter { it.value > 1 }
                .keys

        duplicatedAliases.forEach { alias ->
            errors += "Duplicate alias: $alias"
        }

        return KnowledgeImportBatchValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }
}