package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem

class DefaultCatalogMergeStrategy : CatalogMergeStrategy {

    override fun merge(
        existingItem: CatalogItem,
        importedItem: CatalogItem
    ): CatalogItem {

        return existingItem.copy(
            knowledge = importedItem.knowledge ?: existingItem.knowledge,
            colloquial = (
                    existingItem.colloquial + importedItem.colloquial
                    ).distinct()
        )
    }
}