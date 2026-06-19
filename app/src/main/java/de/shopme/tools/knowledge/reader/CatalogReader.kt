package de.shopme.tools.knowledge.reader

import de.shopme.domain.catalog.CatalogItem

interface CatalogReader {

    fun read(): List<CatalogItem>

}