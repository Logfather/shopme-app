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

        val addedCatalogItems =
            createAddedCatalogItems(
                catalog = catalog,
                patch = patch
            )

        val updatedCatalogItems =
            applyUpdateOperations(
                catalog = catalog,
                patch = patch
            )

        return sortCatalog(
            updatedCatalogItems + addedCatalogItems
        )
    }

    private fun createAddedCatalogItems(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    ): List<CatalogItem> {

        val existingNormalizedNames =
            catalog
                .map { item -> item.normalized }
                .toSet()

        return patch.operations
            .filter { operation ->
                operation.type == FoodsJsonPatchOperationType.ADD
            }
            .filterNot { operation ->
                val normalizedName =
                    operation.catalogNormalizedName()

                existingNormalizedNames.contains(normalizedName)
            }
            .map { operation ->
                operation.toCatalogItem()
            }
    }

    private fun applyUpdateOperations(
        catalog: List<CatalogItem>,
        patch: FoodsJsonPatch
    ): List<CatalogItem> {

        val updatesByNormalizedName =
            patch.operations
                .filter { operation ->
                    operation.type == FoodsJsonPatchOperationType.UPDATE
                }
                .associateBy { operation ->
                    operation.catalogNormalizedName()
                }

        if (updatesByNormalizedName.isEmpty()) {
            return catalog
        }

        return catalog.map { item ->

            val operation =
                updatesByNormalizedName[item.normalized]
                    ?: return@map item

            item.copy(
                itemname = operation.displayName(),
                normalized = operation.catalogNormalizedName(),
                plural = operation.catalogNormalizedName(),
                colloquial = operation.sortedAliases(),
                phonetic_tokens = operation.catalogTokens(),
                autocomplete_tokens = operation.catalogTokens(),
                knowledge = catalogKnowledgeMapper.map(operation.candidate)
            )
        }
    }

    private fun FoodsJsonPatchOperation.toCatalogItem(): CatalogItem {

        val displayName =
            displayName()

        val normalizedName =
            catalogNormalizedName()

        val tokens =
            catalogTokens()

        return CatalogItem(
            itemname = displayName,
            category = "unknown",
            production = "unknown",
            normalized = normalizedName,
            plural = normalizedName,
            colloquial = sortedAliases(),
            phonetic_tokens = tokens,
            autocomplete_tokens = tokens,
            knowledge = catalogKnowledgeMapper.map(candidate)
        )
    }

    private fun FoodsJsonPatchOperation.displayName(): String =
        candidate.aliases
            .firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: canonicalId

    private fun FoodsJsonPatchOperation.catalogNormalizedName(): String =
        displayName().normalizedCatalogName()

    private fun FoodsJsonPatchOperation.catalogTokens(): List<String> =
        candidate.aliases
            .plus(canonicalId)
            .map { value ->
                value.normalizedCatalogName()
            }
            .filter { value ->
                value.isNotBlank()
            }
            .distinct()
            .sorted()

    private fun FoodsJsonPatchOperation.sortedAliases(): List<String> =
        candidate.aliases
            .map { alias ->
                alias.trim()
            }
            .filter { alias ->
                alias.isNotBlank()
            }
            .distinct()
            .sorted()

    private fun String.normalizedCatalogName(): String =
        trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .takeIf { it.isNotBlank() }
            ?: ""

    private fun sortCatalog(
        catalog: List<CatalogItem>
    ): List<CatalogItem> =
        catalog.sortedBy { item ->
            item.normalized
        }
}