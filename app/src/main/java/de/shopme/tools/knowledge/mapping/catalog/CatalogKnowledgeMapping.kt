package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMapping(
    val catalogKey: String,
    val serverKey: String,
    val serverArtifact: String,
    val strategy: CatalogKnowledgeMappingStrategy,
    val confidence: Double,
    val reason: String? = null
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank"
        }

        require(serverKey.isNotBlank()) {
            "serverKey must not be blank"
        }

        require(serverArtifact.isNotBlank()) {
            "serverArtifact must not be blank"
        }

        require(confidence in 0.0..1.0) {
            "confidence must be between 0.0 and 1.0"
        }

        require(
            reason == null ||
                    reason.isNotBlank()
        ) {
            "reason must be null or non-blank"
        }
    }
}