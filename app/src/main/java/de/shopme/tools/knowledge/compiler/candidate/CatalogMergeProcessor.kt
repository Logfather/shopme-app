package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem

class CatalogMergeProcessor(
    private val strategy: CatalogMergeStrategy =
        DefaultCatalogMergeStrategy()
) {

    fun merge(
        existingItems: List<CatalogItem>,
        importedItems: List<CatalogItem>
    ): CatalogMergeResult {

        val existingByNormalized = existingItems.associateBy {
            it.normalized
        }

        val importedByNormalized = importedItems.associateBy {
            it.normalized
        }

        val mergedItems = existingByNormalized.toMutableMap()

        importedByNormalized.forEach { (normalized, importedItem) ->

            val existingItem = mergedItems[normalized]

            mergedItems[normalized] =
                if (existingItem == null) {
                    importedItem
                } else {
                    strategy.merge(
                        existingItem = existingItem,
                        importedItem = importedItem
                    )
                }
        }

        return CatalogMergeResult(
            items = mergedItems.values.sortedBy { it.normalized },
            summary = CatalogMergeSummary(
                existingItems = existingItems.size,
                importedItems = importedItems.size,
                addedItems = importedByNormalized.keys.count { it !in existingByNormalized },
                updatedItems = importedByNormalized.keys.count { it in existingByNormalized },
                mergedItems = mergedItems.size
            )
        )
    }
}