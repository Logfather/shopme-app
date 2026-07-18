package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult

interface AIKnowledgeCatalogResultImporter {
    fun importAIKnowledge(result: AIKnowledgeBuildResult)
}