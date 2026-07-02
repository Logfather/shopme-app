package de.shopme.testing.system.tools.knowledge.ai.off

import de.shopme.tools.knowledge.ai.sources.off.OFFAIImportAdapter
import de.shopme.tools.knowledge.ai.sources.off.OFFRawProduct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class OFFAIImportAdapterTest {

    @Test
    fun mapsAdditionalOffFieldsIntoRawKnowledgeInput() {

        val request =
            OFFAIImportAdapter().adapt(
                listOf(
                    OFFRawProduct(
                        code = "00000758",
                        productName = "Test Food",
                        ingredientsText = "Milk",
                        categories = "Snacks",
                        labels = "Fairtrade, Organic",
                        countries = "Germany",
                        origins = "France",
                        allergens = "en:milk",
                        packaging = "plastic tray",
                        manufacturingPlaces = "Berlin",
                        novaGroup = 4
                    )
                )
            )

        val fields =
            request.inputs.single().fields

        assertEquals("Test Food", fields["name"])
        assertEquals("Milk", fields["ingredients"])
        assertEquals("Snacks", fields["taxonomy"])

        assertEquals("en:milk", fields["allergens"])
        assertEquals("plastic tray", fields["packaging"])
        assertEquals(4, fields["processing"])
        assertEquals("Berlin", fields["production"])

        assertNotNull(fields["locality"])
        assertNotNull(fields["fairtrade"])
        assertNotNull(fields["animalWelfare"])
    }
}