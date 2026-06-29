package de.shopme.testing.system.tools.knowledge.importer.off.ai.openai

import de.shopme.tools.knowledge.ai.openai.OpenAIChatChoice
import de.shopme.tools.knowledge.ai.openai.OpenAIChatMessageResponse
import de.shopme.tools.knowledge.ai.openai.OpenAIChatResponseBody
import de.shopme.tools.knowledge.ai.openai.OpenAIResponseMapper
import org.junit.Assert.assertEquals
import org.junit.Test

class OpenAIChatResponseBodyMapperTest {

    @Test
    fun mapCreatesProviderResponseFromFirstChoice() {

        val mapper = OpenAIResponseMapper()

        val response = OpenAIChatResponseBody(
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

        val result = mapper.map(response)

        assertEquals(
            """{"candidates":[]}""",
            result.content
        )
    }
}