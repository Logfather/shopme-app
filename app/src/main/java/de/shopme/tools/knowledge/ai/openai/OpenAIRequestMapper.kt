package de.shopme.tools.knowledge.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.AIProviderRequest

class OpenAIRequestMapper(
    private val config: AIProviderConfig
) {

    fun map(
        request: AIProviderRequest
    ): OpenAIRequest {

        return OpenAIRequest(
            model = config.model,
            systemPrompt = request.systemPrompt,
            userPrompt = request.userPrompt
        )
    }
}