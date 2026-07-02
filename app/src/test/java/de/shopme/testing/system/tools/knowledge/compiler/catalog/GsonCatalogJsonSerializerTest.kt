package de.shopme.testing.system.tools.knowledge.compiler.catalog

import com.google.gson.Gson
import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.catalog.GsonCatalogJsonSerializer
import org.junit.Assert.assertTrue
import org.junit.Test

class GsonCatalogJsonSerializerTest {

    @Test
    fun serializeContainsCatalogItemData() {

        val serializer = GsonCatalogJsonSerializer(
            gson = Gson()
        )

        val json = serializer.serialize(
            listOf(
                catalogItem("apple")
            )
        )

        assertTrue(
            json.contains("apple")
        )
    }

    private fun catalogItem(
        normalized: String
    ): CatalogItem {

        return CatalogItem(
            itemname = normalized,
            category = "",
            production = "",
            normalized = normalized,
            plural = "",
            colloquial = emptyList(),
            phonetic_tokens = emptyList(),
            autocomplete_tokens = emptyList()
        )
    }
}