package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult

class AIKnowledgeResultCatalogWriter(
    private val importWorkflow: AIKnowledgeCatalogImportWorkflow
) : AIKnowledgeCatalogResultImporter {

    override fun importAIKnowledge(
        result: AIKnowledgeBuildResult
    ) {
        importWorkflow.importAIKnowledge(
            catalog = emptyList(),
            result = result
        )
    }
}