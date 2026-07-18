package de.shopme.tools.knowledge.mapping.catalog

interface CatalogKnowledgeMappingBuilder {

    fun build(
        catalogKeys: Set<String>,
        artifactName: String,
        serverKeys: Sequence<String>
    ): List<CatalogKnowledgeMapping>
}