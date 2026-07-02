package de.shopme.testing.system.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.catalog.CatalogJsonDeserializer
import de.shopme.tools.knowledge.compiler.catalog.FileCatalogReader
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class FileCatalogReaderTest {

    @Test
    fun readReturnsCatalogFromFile() {

        val file = File.createTempFile(
            "catalog",
            ".json"
        )

        try {
            file.writeText("TEST_JSON")

            val reader = FileCatalogReader(
                deserializer = FakeCatalogJsonDeserializer(),
                inputFile = file
            )

            val catalog = reader.read()

            assertEquals(
                listOf(
                    "apple"
                ),
                catalog.map {
                    it.normalized
                }
            )

        } finally {
            file.delete()
        }
    }

    private class FakeCatalogJsonDeserializer :
        CatalogJsonDeserializer {

        override fun deserialize(
            json: String
        ): List<CatalogItem> {

            assertEquals(
                "TEST_JSON",
                json
            )

            return listOf(
                catalogItem("apple")
            )
        }
    }

    companion object {

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
}