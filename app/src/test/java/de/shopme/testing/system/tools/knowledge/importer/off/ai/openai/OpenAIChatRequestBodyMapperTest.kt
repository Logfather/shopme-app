package de.shopme.testing.system.tools.knowledge.importer.off.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.openai.OpenAIChatRequestBodyMapper
import de.shopme.tools.knowledge.ai.openai.OpenAIRequest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertNotNull

class OpenAIChatRequestBodyMapperTest {

    @Test
    fun mapCreatesChatRequestBodyFromOpenAIRequest() {

        val mapper =
            OpenAIChatRequestBodyMapper(
                config =
                    AIProviderConfig(
                        providerName = "openai",
                        model = "gpt-test",
                        apiKey = "test-key",
                        endpoint = "test-endpoint",
                        temperature = 0.0
                    )
            )

        val result =
            mapper.map(
                OpenAIRequest(
                    model = "gpt-test",
                    systemPrompt = "System prompt",
                    userPrompt = "User prompt"
                )
            )

        assertEquals(
            "gpt-test",
            result.model
        )

        val temperature =
            assertNotNull(
                result.temperature
            )

        assertEquals(
            0.0,
            temperature,
            0.0
        )

        assertEquals(
            2,
            result.messages.size
        )

        assertEquals(
            "system",
            result.messages[0].role
        )

        assertEquals(
            "System prompt",
            result.messages[0].content
        )

        assertEquals(
            "user",
            result.messages[1].role
        )

        assertEquals(
            "User prompt",
            result.messages[1].content
        )
    }
}