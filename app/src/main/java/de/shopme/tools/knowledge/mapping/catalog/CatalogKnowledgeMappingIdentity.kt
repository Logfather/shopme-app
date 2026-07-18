package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMappingIdentity(
    val catalogKey: String,
    val serverArtifact: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank"
        }

        require(serverArtifact.isNotBlank()) {
            "serverArtifact must not be blank"
        }
    }
}