package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem

interface CatalogJsonDeserializer {

    fun deserialize(
        json: String
    ): List<CatalogItem>
}