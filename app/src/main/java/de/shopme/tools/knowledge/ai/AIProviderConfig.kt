package de.shopme.tools.knowledge.ai

data class AIProviderConfig(
    val providerName: String,
    val model: String,
    val apiKey: String,
    val endpoint: String,
    val temperature: Double? = null
)