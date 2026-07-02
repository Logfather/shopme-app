package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ai.catalog.CatalogKnowledgeMapper

class DefaultFoodsJsonPatchApplier : FoodsJsonPatchApplier {

    private val catalogKnowledgeMapper: CatalogKnowledgeMapper =
        CatalogKnowledgeMapper()

    override fun apply(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    ): List<CatalogItem> {

        if (patch.operations.isEmpty()) {
            return sortCatalog(catalog)
        }

        val addedCatalogItems = createAddedCatalogItems(
            catalog = catalog,
            patch = patch
        )

        val updatedCatalogItems = applyUpdateOperations(
            catalog = catalog,
            patch = patch
        )

        val updatedCatalog = mergeCatalogItems(
            updatedCatalogItems = updatedCatalogItems,
            addedCatalogItems = addedCatalogItems
        )

        return sortCatalog(updatedCatalog)
    }

    private fun createAddedCatalogItems(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    ): List<CatalogItem> {

        val existingNormalizedNames = catalog
            .map { it.normalized }
            .toSet()

        return patch.operations
            .filter { operation ->
                operation.type == FoodsJsonPatchOperationType.ADD
            }
            .filterNot { operation ->
                existingNormalizedNames.contains(operation.canonicalId)
            }
            .map { operation ->
                operation.toCatalogItem()
            }
    }

    private fun applyUpdateOperations(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    ): List<CatalogItem> {

        val updatesByCanonicalId = patch.operations
            .filter { operation ->
                operation.type == FoodsJsonPatchOperationType.UPDATE
            }
            .associateBy { operation ->
                operation.canonicalId
            }

        if (updatesByCanonicalId.isEmpty()) {
            return catalog
        }

        return catalog.map { item ->

            val operation = updatesByCanonicalId[item.normalized]
                ?: return@map item

            item.copy(
                colloquial = operation.candidate.aliases.toList(),
                knowledge = catalogKnowledgeMapper.map(operation.candidate)
            )
        }
    }

    private fun FoodsJsonPatchOperation.toCatalogItem(): CatalogItem {

        val displayName = displayName()
        val tokens = catalogTokens()

        return CatalogItem(
            itemname = displayName,
            category = "unknown",
            production = "unknown",
            normalized = canonicalId,
            plural = canonicalId,
            colloquial = sortedAliases(),
            phonetic_tokens = tokens,
            autocomplete_tokens = tokens,
            knowledge = catalogKnowledgeMapper.map(candidate)
        )
    }

    private fun sortCatalog(
        catalog: List<CatalogItem>
    ): List<CatalogItem> {

        return catalog.sortedBy {
            it.normalized
        }
    }

    private fun mergeCatalogItems(
        updatedCatalogItems: List<CatalogItem>,
        addedCatalogItems: List<CatalogItem>
    ): List<CatalogItem> {

        return updatedCatalogItems + addedCatalogItems
    }

    private fun containsCatalogItem(
        catalog: List<CatalogItem>,
        normalized: String
    ): Boolean {

        return catalog.any {
            it.normalized == normalized
        }
    }

    private fun FoodsJsonPatchOperation.displayName(): String =
        candidate.aliases
            .firstOrNull()
            ?: canonicalId

    private fun FoodsJsonPatchOperation.catalogTokens(): List<String> =
        candidate.aliases
            .plus(canonicalId)
            .map { it.lowercase() }
            .distinct()
            .sorted()

    private fun FoodsJsonPatchOperation.sortedAliases(): List<String> =
        candidate.aliases
            .toList()
            .sorted()
}