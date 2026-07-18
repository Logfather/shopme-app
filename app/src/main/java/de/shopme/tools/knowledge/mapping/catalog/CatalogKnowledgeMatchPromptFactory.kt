package de.shopme.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProviderRequest

interface CatalogKnowledgeMatchPromptFactory {

    fun create(
        request: CatalogKnowledgeMatchRequest
    ): AIProviderRequest
}