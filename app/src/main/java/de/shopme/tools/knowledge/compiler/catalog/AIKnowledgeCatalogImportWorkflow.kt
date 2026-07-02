package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult

interface AIKnowledgeCatalogImportWorkflow {

    fun importAIKnowledge(
        catalog: List<CatalogItem>,
        result: AIKnowledgeBuildResult
    )
}