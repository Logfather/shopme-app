package de.shopme.domain.catalog.model

data class KnowledgeReference(
    val reference: String,
    val source: String,
    val value: String? = null,

)