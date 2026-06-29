package de.shopme.tools.knowledge.ai.builder

interface AIKnowledgeBuilder {

    fun build(
        request: AIKnowledgeBuildRequest
    ): AIKnowledgeBuildResult
}