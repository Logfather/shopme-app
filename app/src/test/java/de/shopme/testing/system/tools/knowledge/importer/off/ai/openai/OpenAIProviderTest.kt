package de.shopme.testing.system.tools.knowledge.importer.off.ai.openai

import de.shopme.tools.knowledge.ai.AIProviderConfig
import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.openai.OpenAIChatChoice
import de.shopme.tools.knowledge.ai.openai.OpenAIChatMessageResponse
import de.shopme.tools.knowledge.ai.openai.OpenAIChatResponseBody
import de.shopme.tools.knowledge.ai.openai.OpenAIChatResponseBodyDeserializer
import de.shopme.tools.knowledge.ai.openai.OpenAIHttpClient
import de.shopme.tools.knowledge.ai.openai.OpenAIProvider
import de.shopme.tools.knowledge.ai.openai.OpenAIRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenAIProviderTest {

    @Test
    fun completeMapsRequestCallsHttpClientDeserializesAndMapsResponse() {

        val httpClient = RecordingOpenAIHttpClient(
            rawResponse = """{"result":"ok"}"""
        )

        val deserializer = RecordingOpenAIChatResponseBodyDeserializer()

        val provider = OpenAIProvider(
            config = AIProviderConfig(
                providerName = "openai",
                model = "gpt-test",
                apiKey = "test-key",
                endpoint = "test-endpoint",
                temperature = 0.0
            ),
            httpClient = httpClient,
            deserializer = deserializer
        )

        val result = provider.complete(
            AIProviderRequest(
                systemPrompt = "System prompt",
                userPrompt = "User prompt"
            )
        )

        assertTrue(httpClient.wasCalled)
        assertTrue(deserializer.wasCalled)

        assertEquals("gpt-test", httpClient.request?.model)
        assertEquals("System prompt", httpClient.request?.systemPrompt)
        assertEquals("User prompt", httpClient.request?.userPrompt)

        assertEquals(
            """{"result":"ok"}""",
            deserializer.content
        )

        assertEquals(
            """{"candidates":[]}""",
            result.content
        )
    }

    private class RecordingOpenAIHttpClient(
        private val rawResponse: String
    ) : OpenAIHttpClient {

        var wasCalled: Boolean = false
        var request: OpenAIRequest? = null

        override fun complete(
            request: OpenAIRequest
        ): String {

            wasCalled = true
            this.request = request

            return rawResponse
        }
    }

    private class RecordingOpenAIChatResponseBodyDeserializer :
        OpenAIChatResponseBodyDeserializer {

        var wasCalled: Boolean = false
        var content: String? = null

        override fun deserialize(
            content: String
        ): OpenAIChatResponseBody {

            wasCalled = true
            this.content = content

            return OpenAIChatResponseBody(
                id = "chatcmpl-test",
                choices = listOf(
                    OpenAIChatChoice(
                        message = OpenAIChatMessageResponse(
                            role = "assistant",
                            content = """{"candidates":[]}"""
                        )
                    )
                )
            )
        }
    }
}