package de.shopme.tools.knowledge.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.AIProviderRequest

object OpenAIIntegrationRunner {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {

        val openAIConfig = OpenAIProviderConfig.fromEnvironment()

        val config = AIProviderConfig(
            providerName = "openai",
            model = openAIConfig.model,
            apiKey = openAIConfig.apiKey,
            endpoint = openAIConfig.endpoint,
            temperature = 0.0
        )

        val provider = OpenAIProvider(
            config = config,
            httpClient = RealOpenAIHttpClient(
                config = config
            )
        )

        val response = provider.complete(
            AIProviderRequest(
                systemPrompt = "Say only: connection successful.",
                userPrompt = "Ping"
            )
        )

        println(response.content)
    }
}