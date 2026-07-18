package de.shopme.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProvider

class OpenAICatalogKnowledgeMatcherFactory(
    private val openAIProvider: AIProvider
) {

    fun create(): CatalogKnowledgeMatcher =
        AIProviderCatalogKnowledgeMatcher(
            aiProvider =
                openAIProvider,
            promptFactory =
                DefaultCatalogKnowledgeMatchPromptFactory(),
            responseParser =
                DefaultCatalogKnowledgeMatchResponseParser()
        )
}