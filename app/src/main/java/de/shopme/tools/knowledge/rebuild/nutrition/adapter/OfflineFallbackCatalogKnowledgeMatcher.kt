package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatcher

class OfflineFallbackCatalogKnowledgeMatcher :
    CatalogKnowledgeMatcher {

    override fun match(
        request: CatalogKnowledgeMatchRequest
    ): CatalogKnowledgeMatchDecision {

        throw GptFallbackRequiredException(
            catalogKey =
                request.catalogKey
        )
    }
}