package de.shopme.testing.system.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.catalog.CatalogJsonSerializer
import de.shopme.tools.knowledge.compiler.catalog.JsonCatalogWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class JsonCatalogWriterTest {

    @Test
    fun writeWritesSerializedCatalogToFile() {

        val file = File.createTempFile(
            "catalog",
            ".json"
        )

        try {

            val writer = JsonCatalogWriter(
                serializer = FakeCatalogJsonSerializer(),
                outputFile = file
            )

            writer.write(
                listOf(
                    catalogItem("apple")
                )
            )

            assertTrue(file.exists())

            assertEquals(
                "TEST_JSON",
                file.readText()
            )

        } finally {
            file.delete()
        }
    }

    private class FakeCatalogJsonSerializer :
        CatalogJsonSerializer {

        override fun serialize(
            catalog: List<CatalogItem>
        ): String {

            return "TEST_JSON"
        }
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