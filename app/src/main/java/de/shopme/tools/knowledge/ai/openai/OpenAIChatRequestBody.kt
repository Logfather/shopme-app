package de.shopme.tools.knowledge.ai.openai

data class OpenAIChatRequestBody(
    val model: String,
    val messages: List<OpenAIChatMessage>,
    val temperature: Double? = null
)