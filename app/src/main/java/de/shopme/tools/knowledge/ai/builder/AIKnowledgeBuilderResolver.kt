package de.shopme.tools.knowledge.ai.builder

interface AIKnowledgeBuilderResolver {

    fun resolve(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuilder
}