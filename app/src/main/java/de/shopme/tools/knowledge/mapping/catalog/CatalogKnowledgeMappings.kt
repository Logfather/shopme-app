package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMappings(
    val version: Int,
    val mappings: List<CatalogKnowledgeMapping>
) {

    init {
        require(version > 0) {
            "version must be greater than zero"
        }

        requireNoDuplicateMappings()
    }


    private fun requireNoDuplicateMappings() {

        val duplicates =
            mappings
                .groupBy { mapping ->
                    MappingIdentity(
                        catalogKey = mapping.catalogKey,
                        serverArtifact = mapping.serverArtifact
                    )
                }
                .filterValues { mappings ->
                    mappings.size > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "Duplicate catalog knowledge mappings: " +
                    duplicates.joinToString {
                        "${it.catalogKey} @ ${it.serverArtifact}"
                    }
        }
    }


    private data class MappingIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )
}