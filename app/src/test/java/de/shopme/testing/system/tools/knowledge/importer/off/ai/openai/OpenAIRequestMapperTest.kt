package de.shopme.testing.system.tools.knowledge.importer.off.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.openai.OpenAIRequestMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAIRequestMapperTest {

    @Test
    fun mapCreatesOpenAIRequestFromProviderRequestAndConfig() {

        val mapper = OpenAIRequestMapper(
            config = AIProviderConfig(
                providerName = "openai",
                model = "gpt-test",
                apiKey = "test-key",
                endpoint = "test-endpoint",
                temperature = 0.0
            )
        )

        val result = mapper.map(
            AIProviderRequest(
                systemPrompt = "System prompt",
                userPrompt = "User prompt"
            )
        )

        assertEquals("gpt-test", result.model)
        assertEquals("System prompt", result.systemPrompt)
        assertEquals("User prompt", result.userPrompt)
    }
}