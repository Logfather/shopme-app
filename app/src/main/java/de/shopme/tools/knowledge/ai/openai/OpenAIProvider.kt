package de.shopme.tools.knowledge.ai.openai

import de.shopme.tools.knowledge.ai.AIProvider
import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.AIProviderResponse

class OpenAIProvider(
    private val config: AIProviderConfig,
    private val requestMapper: OpenAIRequestMapper =
        OpenAIRequestMapper(config),
    private val chatRequestBodyMapper: OpenAIChatRequestBodyMapper =
        OpenAIChatRequestBodyMapper(config),
    private val httpClient: OpenAIHttpClient =
        PlaceholderOpenAIHttpClient(),
    private val deserializer: OpenAIChatResponseBodyDeserializer =
        JsonOpenAIChatResponseBodyDeserializer(),
    private val responseMapper: OpenAIResponseMapper =
        OpenAIResponseMapper()
) : AIProvider {

    override fun complete(
        request: AIProviderRequest
    ): AIProviderResponse {

        val openAIRequest = requestMapper.map(request)

        val rawResponse = httpClient.complete(openAIRequest)

        val responseBody = deserializer.deserialize(
            rawResponse
        )

        return responseMapper.map(responseBody)
    }
}