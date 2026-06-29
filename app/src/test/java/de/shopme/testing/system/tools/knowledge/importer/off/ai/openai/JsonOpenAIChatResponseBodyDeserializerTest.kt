package de.shopme.testing.system.tools.knowledge.importer.off.ai.openai

import de.shopme.tools.knowledge.ai.openai.JsonOpenAIChatResponseBodyDeserializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class JsonOpenAIChatResponseBodyDeserializerTest {

    @Test
    fun deserializeParsesOpenAIChatResponseBody() {

        val deserializer = JsonOpenAIChatResponseBodyDeserializer()

        val json = """
            {
              "id": "chatcmpl-123",
              "choices": [
                {
                  "message": {
                    "role": "assistant",
                    "content": "{\"candidates\":[]}"
                  }
                }
              ]
            }
        """.trimIndent()

        val response = deserializer.deserialize(json)

        assertEquals(
            "chatcmpl-123",
            response.id
        )

        assertEquals(
            1,
            response.choices.size
        )

        val message = response
            .choices
            .first()
            .message

        assertEquals(
            "assistant",
            message.role
        )

        assertEquals(
            """{"candidates":[]}""",
            message.content
        )

        assertNotNull(message)
    }
}