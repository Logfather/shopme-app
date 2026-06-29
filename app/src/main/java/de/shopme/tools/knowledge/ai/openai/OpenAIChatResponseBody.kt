package de.shopme.tools.knowledge.ai.openai

data class OpenAIChatResponseBody(
    val id: String,
    val choices: List<OpenAIChatChoice>
)