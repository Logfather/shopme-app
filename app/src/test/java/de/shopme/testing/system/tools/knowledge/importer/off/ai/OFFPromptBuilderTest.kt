package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionBatch
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionInput
import de.shopme.tools.knowledge.importer.off.ai.OFFPromptBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class OFFPromptBuilderTest {

    @Test
    fun buildCreatesSystemAndUserPromptFromOFFBatch() {

        val builder = OFFPromptBuilder()

        val request = builder.build(
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

        assertTrue(
            request.systemPrompt.contains("canonical food knowledge candidates")
        )

        assertTrue(request.userPrompt.contains("code=123"))
        assertTrue(request.userPrompt.contains("productName=Apple"))
        assertTrue(request.userPrompt.contains("productNameDe=Apfel"))
        assertTrue(request.userPrompt.contains("brands=TestBrand"))
        assertTrue(request.userPrompt.contains("categories=Fruits"))
        assertTrue(request.userPrompt.contains("ingredientsText=Apple"))
        assertTrue(request.userPrompt.contains("ingredientsTextDe=Apfel"))
        assertTrue(request.userPrompt.contains("labels=Bio"))
        assertTrue(request.userPrompt.contains("countries=Germany"))
        assertTrue(request.userPrompt.contains("quantity=1 kg"))
    }

    @Test
    fun buildUsesEmptyStringsForMissingValues() {

        val builder = OFFPromptBuilder()

        val request = builder.build(
            OFFAIExtractionBatch(
                source = "Open Food Facts",
                sourceVersion = null,
                products = listOf(
                    OFFAIExtractionInput(
                        code = null,
                        productName = null,
                        productNameDe = null,
                        brands = null,
                        categories = null,
                        ingredientsText = null,
                        ingredientsTextDe = null,
                        labels = null,
                        countries = null,
                        quantity = null
                    )
                )
            )
        )

        assertTrue(request.userPrompt.contains("code="))
        assertTrue(request.userPrompt.contains("productName="))
        assertTrue(request.userPrompt.contains("productNameDe="))
        assertTrue(request.userPrompt.contains("brands="))
        assertTrue(request.userPrompt.contains("categories="))
        assertTrue(request.userPrompt.contains("ingredientsText="))
        assertTrue(request.userPrompt.contains("ingredientsTextDe="))
        assertTrue(request.userPrompt.contains("labels="))
        assertTrue(request.userPrompt.contains("countries="))
        assertTrue(request.userPrompt.contains("quantity="))
    }
}