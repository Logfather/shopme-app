package de.shopme.tools.knowledge.ai.openai

data class OpenAIRequest(
    val model: String,
    val systemPrompt: String,
    val userPrompt: String
)