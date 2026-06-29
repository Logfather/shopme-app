package de.shopme.tools.knowledge.dimension.explorer

import de.shopme.domain.catalog.CatalogItem

interface KnowledgeExplorerProvider {

    fun create(
        catalogItem: CatalogItem
    ): KnowledgeExplorerModel
}