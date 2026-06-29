package de.shopme.testing.system.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionBatch
import de.shopme.tools.knowledge.importer.off.ai.OFFAIExtractionInput
import de.shopme.tools.knowledge.importer.off.ai.OFFAIKnowledgeBuildRequestMapper
import junit.framework.TestCase.assertEquals
import kotlin.test.Test

class OFFAIKnowledgeBuildRequestMapperTest {

    @Test
    fun mapBatchToBuildRequest() {

        val batch = OFFAIExtractionBatch(
            source = "open_food_facts",
            sourceVersion = "2026-06",
            products = listOf(
                OFFAIExtractionInput(
                    code = "123",
                    productName = "Apple",
                    productNameDe = "Apfel",
                    brands = null,
                    categories = "Fruit",
                    ingredientsText = "Apple",
                    ingredientsTextDe = "Apfel",
                    labels = null,
                    countries = "Germany",
                    quantity = "1 piece"
                )
            )
        )

        val request = OFFAIKnowledgeBuildRequestMapper().map(batch)

        assertEquals("open_food_facts", request.source.name)
        assertEquals(1, request.inputs.size)
        assertEquals("123", request.inputs.first().sourceId)
        assertEquals(
            "Apple",
            request.inputs.first().fields["productName"]
        )
    }
}