package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch

class FileCatalogUpdateWorkflow(
    private val reader: FileCatalogReader,
    private val updateWorkflow: AIKnowledgeCatalogUpdateWorkflow,
    private val importWorkflow: AIKnowledgeCatalogImportWorkflow? = null
){

    fun applyPatch(
        patch: FoodsJsonPatch
    ) {

        val catalog =
            reader.read()

        updateWorkflow.updateCatalog(
            catalog = catalog,
            patch = patch
        )
    }

    fun importAIKnowledge(
        result: AIKnowledgeBuildResult
    ) {

        val catalog = reader.read()

        requireNotNull(importWorkflow) {
            "AIKnowledgeCatalogImportWorkflow not configured."
        }.importAIKnowledge(
            catalog = catalog,
            result = result
        )
    }
}