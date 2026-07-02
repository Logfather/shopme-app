package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem

interface CatalogWriter {

    fun write(
        catalog: List<CatalogItem>
    )
}