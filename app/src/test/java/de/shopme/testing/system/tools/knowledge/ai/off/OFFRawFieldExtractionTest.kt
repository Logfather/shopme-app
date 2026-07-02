package de.shopme.testing.system.tools.knowledge.ai.off

import de.shopme.tools.knowledge.ai.sources.off.OFFJsonProduct
import org.junit.Assert.assertEquals
import org.junit.Test

class OFFRawFieldExtractionTest {

    @Test
    fun mapsAdditionalOffJsonFieldsToRawProduct() {

        val product =
            OFFJsonProduct(
                code = "00000758",
                product_name = "Test Food",
                generic_name = "Generic Test Food",
                brands = "Test Brand",
                categories = "Snacks",
                ingredients_text = "Milk, sugar",
                labels = "Fairtrade, Organic",
                countries = "Germany",
                origins = "France",
                allergens = "en:milk",
                packaging = "plastic tray",
                manufacturing_places = "Berlin",
                nutrition_grade_fr = "b",
                nova_group = 4,
                nutriments = null
            )

        val raw =
            product.toRawProduct()

        assertEquals("00000758", raw.code)
        assertEquals("Test Food", raw.productName)
        assertEquals("France", raw.origins)
        assertEquals("en:milk", raw.allergens)
        assertEquals("plastic tray", raw.packaging)
        assertEquals("Berlin", raw.manufacturingPlaces)
        assertEquals(4, raw.novaGroup)
    }
}