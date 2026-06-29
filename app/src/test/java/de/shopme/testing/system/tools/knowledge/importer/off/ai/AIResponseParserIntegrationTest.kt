package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AIResponseParserIntegrationTest {

    @Test
    fun parseCreatesKnowledgeImportBatchFromCanonicalCandidateJson() {

        val parser = JsonAIProviderResponseParser()

        val json = """
            {
              "schemaVersion": "canonical_knowledge_candidate_response_v1",
              "candidates": [
                {
                  "canonicalId": "apple",
                  "aliases": [
                    "apple",
                    "apfel"
                  ],
                  "dimensions": [
                    {
                      "type": "TAXONOMY",
                      "payload": "fruit"
                    }
                  ],
                  "metadata": {
                    "source": "open_food_facts",
                    "confidence": 1.0
                  }
                }
              ]
            }
        """.trimIndent()

        val result = parser.parse(
            AIProviderResponse(
                content = json
            )
        )

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals("apple", candidate.canonicalId)

        assertTrue(candidate.aliases.contains("apple"))
        assertTrue(candidate.aliases.contains("apfel"))

        assertEquals(1, candidate.dimensions.size)

        val dimension = candidate.dimensions.first()

        assertEquals(
            KnowledgeDimensionCandidateType.TAXONOMY,
            dimension.dimension
        )

        assertEquals(
            "fruit",
            dimension.payload
        )

        assertEquals(
            "open_food_facts",
            candidate.metadata.source
        )

        assertEquals(
            1.0,
            candidate.metadata.confidence,
            0.0
        )

        assertEquals(
            "canonical_knowledge_candidate_response_v1",
            candidate.metadata.version
        )

        assertEquals(
            "canonical_knowledge_candidate_response_v1",
            result.metadata.promptVersion
        )
    }
}