package de.shopme.testing.system.tools.knowledge.ai.sources.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.sources.off.OFFAIImportAdapter
import de.shopme.tools.knowledge.ai.sources.off.OFFRawProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class OFFAIImportAdapterTest {

    @Test
    fun adaptCreatesAIKnowledgeBuildRequestFromOpenFoodFactsProducts() {

        val adapter =
            OFFAIImportAdapter()

        val product =
            OFFRawProduct(
                code = "123456",
                productName = "Banana",
                categories = "Fruits",
                ingredientsText = "Banana",
                nutritionGradeFr = "a",
                energyKcal100g = 89.0,
                sugars100g = 12.2
            )

        val request =
            adapter.adapt(
                products = listOf(product)
            )

        assertEquals(
            AIKnowledgeSourceType.OPEN_FOOD_FACTS,
            request.source.type
        )

        assertEquals(
            "Open Food Facts",
            request.source.name
        )

        assertEquals(
            1,
            request.inputs.size
        )

        val input =
            request.inputs.single()

        assertEquals(
            "123456",
            input.sourceId
        )

        assertEquals(
            "Banana",
            input.fields["name"]
        )

        assertEquals(
            "Fruits",
            input.fields["categories"]
        )

        @Suppress("UNCHECKED_CAST")
        val nutrition =
            input.fields["nutrition"] as Map<String, Any?>

        assertEquals(
            89.0,
            nutrition["energyKcal100g"]
        )

        assertEquals(
            12.2,
            nutrition["sugars100g"]
        )

        assertFalse(
            input.fields.containsKey("brands")
        )

        assertEquals(
            product.ingredientsText,
            input.fields["ingredients"]
        )

        assertEquals(
            product.categories,
            input.fields["taxonomy"]
        )
    }
}