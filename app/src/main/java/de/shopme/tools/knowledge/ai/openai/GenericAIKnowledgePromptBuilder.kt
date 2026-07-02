package de.shopme.tools.knowledge.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest

interface GenericAIKnowledgePromptBuilder {

    fun build(
        request: AIKnowledgeBuildRequest
    ): AIProviderRequest
}