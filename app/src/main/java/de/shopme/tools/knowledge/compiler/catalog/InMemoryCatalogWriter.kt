package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem

class InMemoryCatalogWriter : CatalogWriter {

    private var writtenCatalog: List<CatalogItem> = emptyList()

    override fun write(
        catalog: List<CatalogItem>
    ) {
        writtenCatalog = catalog
    }

    fun readWrittenCatalog(): List<CatalogItem> =
        writtenCatalog
}