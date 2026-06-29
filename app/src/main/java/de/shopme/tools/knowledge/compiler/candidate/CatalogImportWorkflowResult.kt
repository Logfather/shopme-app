package de.shopme.tools.knowledge.compiler.candidate

data class CatalogImportWorkflowResult(
    val isSuccess: Boolean,
    val errors: List<String>,
    val mergeResult: CatalogMergeResult?
)