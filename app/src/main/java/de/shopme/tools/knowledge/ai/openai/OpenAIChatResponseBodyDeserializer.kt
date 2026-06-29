package de.shopme.tools.knowledge.ai.openai

interface OpenAIChatResponseBodyDeserializer {

    fun deserialize(
        content: String
    ): OpenAIChatResponseBody
}