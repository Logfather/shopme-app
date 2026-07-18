package de.shopme.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProvider

class AIProviderCatalogKnowledgeMatcher(
    private val aiProvider: AIProvider,
    private val promptFactory: CatalogKnowledgeMatchPromptFactory,
    private val responseParser: CatalogKnowledgeMatchResponseParser
) : CatalogKnowledgeMatcher {

    override fun match(
        request: CatalogKnowledgeMatchRequest
    ): CatalogKnowledgeMatchDecision {

        val aiRequest =
            promptFactory.create(request)

        val aiResponse =
            aiProvider.complete(aiRequest)

        return responseParser.parse(
            request = request,
            response = aiResponse
        )
    }
}