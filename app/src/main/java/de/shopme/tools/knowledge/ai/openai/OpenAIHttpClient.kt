package de.shopme.tools.knowledge.ai.openai

interface OpenAIHttpClient {

    fun complete(
        request: OpenAIRequest
    ): String
}