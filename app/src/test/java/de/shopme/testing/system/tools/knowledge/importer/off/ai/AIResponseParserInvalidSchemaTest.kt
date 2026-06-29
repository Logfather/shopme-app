package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import org.junit.Test

class AIResponseParserInvalidSchemaTest {

    @Test(
        expected = IllegalArgumentException::class
    )
    fun parseRejectsUnknownSchemaVersion() {

        val parser = JsonAIProviderResponseParser()

        val json = """
            {
              "schemaVersion": "unknown-schema-v99",
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