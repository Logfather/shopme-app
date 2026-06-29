package de.shopme.tools.knowledge.ai

data class AIProviderRequest(
    val systemPrompt: String,
    val userPrompt: String
)