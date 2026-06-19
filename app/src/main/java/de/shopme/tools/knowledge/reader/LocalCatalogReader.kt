package de.shopme.tools.knowledge.reader

import de.shopme.data.datasource.catalog.CatalogLoader
import de.shopme.domain.catalog.CatalogItem

class LocalCatalogReader(
    private val catalogLoader: CatalogLoader
) : CatalogReader {

    override fun read(): List<CatalogItem> =
        catalogLoader.load()
}