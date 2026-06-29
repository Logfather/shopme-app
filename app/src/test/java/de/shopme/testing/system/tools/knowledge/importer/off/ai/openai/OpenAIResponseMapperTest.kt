package de.shopme.testing.system.tools.knowledge.importer.off.ai.openai

import de.shopme.tools.knowledge.ai.openai.OpenAIResponseMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAIResponseMapperTest {

    @Test
    fun mapWrapsRawResponseIntoAIProviderResponse() {

        val mapper = OpenAIResponseMapper()

        val rawResponse = """
            {
              "id": "chatcmpl-test",
              "choices": [
                {
                  "message": {
                    "content": "{\"candidates\":[]}"
                  }
                }
              ]
            }
        """.trimIndent()

        val result = mapper.map(rawResponse)

        assertEquals(
            rawResponse,
            result.content
        )
    }
}