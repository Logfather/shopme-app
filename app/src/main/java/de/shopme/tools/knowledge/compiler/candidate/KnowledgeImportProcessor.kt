package de.shopme.tools.knowledge.compiler.candidate

class KnowledgeImportProcessor(
    private val validator: KnowledgeImportBatchValidator =
        KnowledgeImportBatchValidator(),
    private val mapper: CanonicalKnowledgeCandidateMapper =
        CanonicalKnowledgeCandidateMapper()
) {

    fun process(
        batch: KnowledgeImportBatch
    ): KnowledgeImportProcessResult {

        val validationResult = validator.validate(batch)

        if (!validationResult.isValid) {
            return KnowledgeImportProcessResult(
                isSuccess = false,
                errors = validationResult.errors,
                acceptedCandidates = emptyList(),
                catalogItems = emptyList()
            )
        }

        val catalogItems = batch.candidates.map { candidate ->
            mapper.map(candidate)
        }

        return KnowledgeImportProcessResult(
            isSuccess = true,
            errors = emptyList(),
            acceptedCandidates = batch.candidates,
            catalogItems = catalogItems
        )
    }
}