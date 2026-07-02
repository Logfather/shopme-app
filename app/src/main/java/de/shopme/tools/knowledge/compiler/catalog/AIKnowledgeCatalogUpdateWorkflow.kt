package de.shopme.tools.knowledge.compiler.catalog

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.compiler.candidate.FoodsJsonPatch

interface AIKnowledgeCatalogUpdateWorkflow {

    fun updateCatalog(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    )
}