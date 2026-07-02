package de.shopme.testing.system.tools.knowledge.compiler.catalog

import com.google.gson.Gson
import de.shopme.tools.knowledge.compiler.catalog.GsonCatalogJsonDeserializer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GsonCatalogJsonDeserializerTest {

    @Test
    fun deserializeReadsCatalogItemData() {

        val deserializer = GsonCatalogJsonDeserializer(
            gson = Gson()
        )

        val json = """
            [
              {
                "itemname":"Apple",
                "category":"fruit",
                "production":"unknown",
                "normalized":"apple",
                "plural":"apples",
                "colloquial":["Apfel"],
                "phonetic_tokens":["apple"],
                "autocomplete_tokens":["apple"],
                "nutritionReference":null,
                "knowledge":{}
              }
            ]
        """.trimIndent()

        val catalog = deserializer.deserialize(json)

        assertEquals(
            1,
            catalog.size
        )

        assertEquals(
            "apple",
            catalog.first().normalized
        )

        assertEquals(
            "Apple",
            catalog.first().itemname
        )

        assertTrue(
            catalog.first().colloquial.contains("Apfel")
        )
    }
}