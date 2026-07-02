package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem

interface CatalogJsonSerializer {

    fun serialize(
        catalog: List<CatalogItem>
    ): String
}