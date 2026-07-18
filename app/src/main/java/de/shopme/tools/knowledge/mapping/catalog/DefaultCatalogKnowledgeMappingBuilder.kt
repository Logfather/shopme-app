package de.shopme.tools.knowledge.mapping.catalog

class DefaultCatalogKnowledgeMappingBuilder :
    CatalogKnowledgeMappingBuilder {

    override fun build(
        catalogKeys: Set<String>,
        artifactName: String,
        serverKeys: Sequence<String>
    ): List<CatalogKnowledgeMapping> {

        require(artifactName.isNotBlank()) {
            "artifactName must not be blank"
        }

        val availableServerKeys =
            serverKeys
                .map(String::trim)
                .filter(String::isNotBlank)
                .toHashSet()

        return catalogKeys
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .filter(availableServerKeys::contains)
            .sorted()
            .map { key ->
                CatalogKnowledgeMapping(
                    catalogKey = key,
                    serverKey = key,
                    serverArtifact = artifactName,
                    strategy =
                        CatalogKnowledgeMappingStrategy.EXACT,
                    confidence = 1.0
                )
            }
            .toList()
    }
}