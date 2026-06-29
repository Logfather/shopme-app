package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem

interface CatalogMergeStrategy {

    fun merge(
        existingItem: CatalogItem,
        importedItem: CatalogItem
    ): CatalogItem
}