package de.shopme.tools.knowledge.ai

data class AIProviderConfig(
    val providerName: String,
    val model: String,
    val apiKey: String? = null,
    val endpoint: String? = null,
    val temperature: Double = 0.0
)