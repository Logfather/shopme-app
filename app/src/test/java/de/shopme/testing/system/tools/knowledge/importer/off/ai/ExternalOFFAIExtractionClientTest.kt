package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIProvider
import de.shopme.tools.knowledge.ai.AIProviderRequest
import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.AIProviderResponseParser
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata
import de.shopme.tools.knowledge.importer.off.ai.ExternalOFFAIExtractionClient
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionBatch
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionInput
import de.shopme.tools.knowledge.importer.off.ai.OFFPromptBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExternalOFFAIExtractionClientTest {

    @Test
    fun extractDelegatesToProviderAndParser() {

        val provider = RecordingAIProvider()
        val parser = RecordingAIProviderResponseParser()

        val client = ExternalOFFAIExtractionClient(
            provider = provider,
            promptBuilder = OFFPromptBuilder(),
            parser = parser
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
        assertTrue(parser.wasCalled)

        assertEquals("test_provider", result.metadata.generatedBy)
        assertEquals(provider.response, parser.response)

        assertTrue(provider.request!!.userPrompt.contains("Apple"))
        assertTrue(provider.request!!.userPrompt.contains("Apfel"))
    }

    private class RecordingAIProvider : AIProvider {

        var wasCalled: Boolean = false
        var request: AIProviderRequest? = null

        val response = AIProviderResponse(
            content = "{}"
        )

        override fun complete(
            request: AIProviderRequest
        ): AIProviderResponse {

            wasCalled = true
            this.request = request

            return response
        }
    }

    private class RecordingAIProviderResponseParser : AIProviderResponseParser {

        var wasCalled: Boolean = false
        var response: AIProviderResponse? = null

        override fun parse(
            response: AIProviderResponse
        ): KnowledgeImportBatch {

            wasCalled = true
            this.response = response

            return KnowledgeImportBatch(
                candidates = emptyList(),
                metadata = KnowledgeImportBatchMetadata(
                    source = "open_food_facts",
                    generatedBy = "test_provider",
                    generatedAt = "test",
                    promptVersion = null
                )
            )
        }
    }
}