package de.shopme.tools.knowledge.compiler.candidate

data class KnowledgeImportBatchValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)