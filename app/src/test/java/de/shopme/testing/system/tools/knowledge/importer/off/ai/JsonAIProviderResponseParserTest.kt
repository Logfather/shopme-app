package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonAIProviderResponseParserTest {

    @Test
    fun parseReturnsEmptyKnowledgeImportBatchForPlaceholderResponse() {

        val parser = JsonAIProviderResponseParser()

        val result = parser.parse(
            AIProviderResponse(
                content = "{}"
            )
        )

        assertNotNull(result)

        assertTrue(result.candidates.isEmpty())

        assertEquals(
            "open_food_facts",
            result.metadata.source
        )

        assertEquals(
            "json_ai_provider_response_parser",
            result.metadata.generatedBy
        )

        assertEquals(
            "placeholder",
            result.metadata.generatedAt
        )

        assertEquals(
            null,
            result.metadata.promptVersion
        )
    }
}