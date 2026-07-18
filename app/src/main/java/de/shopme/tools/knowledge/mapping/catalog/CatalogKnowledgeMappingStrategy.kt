package de.shopme.tools.knowledge.mapping.catalog

enum class CatalogKnowledgeMappingStrategy {

    /**
     * Catalog key and server key are identical.
     */
    EXACT,

    /**
     * Mapping was proposed through semantic model evaluation.
     */
    AI,

    /**
     * Mapping was explicitly curated by a human.
     */
    MANUAL
}