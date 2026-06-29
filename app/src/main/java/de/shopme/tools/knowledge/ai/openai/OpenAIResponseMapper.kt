package de.shopme.tools.knowledge.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderResponse

class OpenAIResponseMapper {

    fun map(
        content: String
    ): AIProviderResponse {

        return AIProviderResponse(
            content = content
        )
    }

    fun map(
        response: OpenAIChatResponseBody
    ): AIProviderResponse {

        val content = response
            .choices
            .first()
            .message
            .content

        return AIProviderResponse(
            content = content
        )
    }


}