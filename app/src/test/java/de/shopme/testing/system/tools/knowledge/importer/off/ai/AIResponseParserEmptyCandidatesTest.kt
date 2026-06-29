package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIResponseParserEmptyCandidatesTest {

    @Test
    fun parseAcceptsEmptyCandidateList() {

        val parser = JsonAIProviderResponseParser()

        val json = """
            {
              "schemaVersion": "canonical_knowledge_candidate_response_v1",
              "candidates": []
            }
        """.trimIndent()

        val result = parser.parse(
            AIProviderResponse(
                content = json
            )
        )

        assertTrue(
            result.candidates.isEmpty()
        )

        assertEquals(
            "canonical_knowledge_candidate_response_v1",
            result.metadata.promptVersion
        )

        assertEquals(
            "ai",
            result.metadata.source
        )

        assertEquals(
            "json-ai-provider-response-parser",
            result.metadata.generatedBy
        )
    }
}