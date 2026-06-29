package de.shopme.tools.knowledge.ai

class FakeAIProvider : AIProvider {

    override fun complete(
        request: AIProviderRequest
    ): AIProviderResponse {

        return AIProviderResponse(
            content = """
            {
              "candidates": []
            }
            """.trimIndent()
        )
    }
}