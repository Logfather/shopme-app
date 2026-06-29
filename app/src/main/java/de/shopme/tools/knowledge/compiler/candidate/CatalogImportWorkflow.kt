package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import java.io.File

class CatalogImportWorkflow(
    private val reader: KnowledgeImportReader =
        JsonKnowledgeImportReader(),
    private val processor: KnowledgeImportProcessor =
        KnowledgeImportProcessor(),
    private val mergeProcessor: CatalogMergeProcessor =
        CatalogMergeProcessor()
) {

    fun import(
        existingItems: List<CatalogItem>,
        importFile: File
    ): CatalogImportWorkflowResult {

        val batch = reader.read(importFile)

        val processResult = processor.process(batch)

        if (!processResult.isSuccess) {
            return CatalogImportWorkflowResult(
                isSuccess = false,
                errors = processResult.errors,
                mergeResult = null
            )
        }

        val mergeResult = mergeProcessor.merge(
            existingItems = existingItems,
            importedItems = processResult.catalogItems
        )

        return CatalogImportWorkflowResult(
            isSuccess = true,
            errors = emptyList(),
            mergeResult = mergeResult
        )
    }
}