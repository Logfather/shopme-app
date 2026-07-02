package de.shopme.testing.system.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.catalog.InMemoryCatalogWriter
import org.junit.Assert.assertEquals
import org.junit.Test

class InMemoryCatalogWriterTest {

    @Test
    fun writeStoresCatalog() {

        val writer = InMemoryCatalogWriter()

        val catalog = listOf(
            catalogItemWithNormalized("apple"),
            catalogItemWithNormalized("banana")
        )

        writer.write(catalog)

        assertEquals(
            catalog,
            writer.readWrittenCatalog()
        )
    }

    private fun catalogItemWithNormalized(
        normalized: String
    ): CatalogItem =
        CatalogItem(
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