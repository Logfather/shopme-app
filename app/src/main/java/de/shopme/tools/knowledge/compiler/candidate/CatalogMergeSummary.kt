package de.shopme.tools.knowledge.compiler.candidate

data class CatalogMergeSummary(
    val existingItems: Int,
    val importedItems: Int,
    val addedItems: Int,
    val updatedItems: Int,
    val mergedItems: Int
)