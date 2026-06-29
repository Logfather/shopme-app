package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProvider
import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import de.shopme.tools.knowledge.importer.off.ai.ExternalOFFAIExtractionClient
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionBatch
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionInput
import de.shopme.tools.knowledge.importer.off.ai.OFFPromptBuilder
import de.shopme.tools.knowledge.importer.off.ai.OFFPromptTemplate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalOFFAIExtractionClientAIResponseIntegrationTest {

    @Test
    fun extractCreatesKnowledgeImportBatchFromOFFBatchAndAIResponse() {

        val provider = CandidateJsonAIProvider()

        val client = ExternalOFFAIExtractionClient(
            provider = provider,
            promptBuilder = OFFPromptBuilder(
                template = OFFPromptTemplate()
            ),
            parser = JsonAIProviderResponseParser()
        )

        val result = client.extract(
            OFFAIExtractionBatch(
                source = "Open Food Facts",
                sourceVersion = "preview",
                products = listOf(
                    OFFAIExtractionInput(
                        code = "123",
                        productName = "Apple",
                        productNameDe = "Apfel",
                        brands = "TestBrand",
                        categories = "Fruits",
                        ingredientsText = "Apple",
                        ingredientsTextDe = "Apfel",
                        labels = "Bio",
                        countries = "Germany",
                        quantity = "1 kg"
                    )
                )
            )
        )

        assertTrue(provider.wasCalled)
        assertTrue(provider.request!!.userPrompt.contains("Apple"))
        assertTrue(provider.request!!.userPrompt.contains("Apfel"))

        assertEquals(1, result.candidates.size)

        val candidate = result.candidates.first()

        assertEquals("apple", candidate.canonicalId)
        assertTrue(candidate.aliases.contains("apple"))
        assertTrue(candidate.aliases.contains("apfel"))

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
    }

    private class CandidateJsonAIProvider : AIProvider {

        var wasCalled: Boolean = false
        var request: AIProviderRequest? = null

        override fun complete(
            request: AIProviderRequest
        ): AIProviderResponse {

            wasCalled = true
            this.request = request

            return AIProviderResponse(
                content = """
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
            )
        }
    }
}