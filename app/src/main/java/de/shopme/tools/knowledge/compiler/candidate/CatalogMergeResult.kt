package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem

data class CatalogMergeResult(
    val items: List<CatalogItem>,
    val summary: CatalogMergeSummary
)