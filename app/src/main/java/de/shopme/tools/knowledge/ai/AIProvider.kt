package de.shopme.tools.knowledge.ai

interface AIProvider {

    fun complete(
        request: AIProviderRequest
    ): AIProviderResponse
}