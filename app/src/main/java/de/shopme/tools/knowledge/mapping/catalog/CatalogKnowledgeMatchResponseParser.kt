package de.shopme.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProviderResponse

interface CatalogKnowledgeMatchResponseParser {

    fun parse(
        request: CatalogKnowledgeMatchRequest,
        response: AIProviderResponse
    ): CatalogKnowledgeMatchDecision
}