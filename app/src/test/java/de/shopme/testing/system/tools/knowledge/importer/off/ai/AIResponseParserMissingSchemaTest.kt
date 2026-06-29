package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import org.junit.Test

class AIResponseParserMissingSchemaTest {

    @Test(
        expected = IllegalArgumentException::class
    )
    fun parseRejectsMissingSchemaVersion() {

        val parser = JsonAIProviderResponseParser()

        val json = """
            {
              "candidates": []
            }
        """.trimIndent()

        parser.parse(
            AIProviderResponse(
                content = json
            )
        )
    }
}