package de.shopme.domain.catalog

data class CatalogItem(
    val itemname: String,
    val category: String,
    val production: String,
    val normalized: String,
    val plural: String,
    val colloquial: List<String>,
    val phonetic_tokens: List<String>,
    val autocomplete_tokens: List<String>
)