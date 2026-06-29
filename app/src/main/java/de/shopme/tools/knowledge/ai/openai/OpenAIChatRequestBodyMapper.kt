package de.shopme.tools.knowledge.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderConfig

class OpenAIChatRequestBodyMapper(
    private val config: AIProviderConfig
) {

    fun map(
        request: OpenAIRequest
    ): OpenAIChatRequestBody {

        return OpenAIChatRequestBody(
            model = request.model,
            temperature = config.temperature,
            messages = listOf(
                OpenAIChatMessage(
                    role = "system",
                    content = request.systemPrompt
                ),
                OpenAIChatMessage(
                    role = "user",
                    content = request.userPrompt
                )
            )
        )
    }
}