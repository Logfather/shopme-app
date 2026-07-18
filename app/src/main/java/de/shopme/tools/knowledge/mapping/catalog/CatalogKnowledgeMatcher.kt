package de.shopme.tools.knowledge.mapping.catalog

interface CatalogKnowledgeMatcher {

    fun match(
        request: CatalogKnowledgeMatchRequest
    ): CatalogKnowledgeMatchDecision

    fun match(
        requests: CatalogKnowledgeMatchRequests
    ): CatalogKnowledgeMatchDecisions =
        CatalogKnowledgeMatchDecisions(
            version =
                CatalogKnowledgeMatchDecisionContract.CURRENT_VERSION,
            decisions =
                requests.requests
                    .map(::match)
        )
}