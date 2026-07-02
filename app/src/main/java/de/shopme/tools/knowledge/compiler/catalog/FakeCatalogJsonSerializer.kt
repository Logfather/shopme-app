package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem

class FakeCatalogJsonSerializer : CatalogJsonSerializer {

    override fun serialize(
        catalog: List<CatalogItem>
    ): String = "TEST_JSON"
}