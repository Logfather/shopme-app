package de.shopme.tools.knowledge.mapping.catalog

data class CatalogServerKnowledgeMapping(
    val catalogKey: String,
    val serverKey: String,
    val sourceArtifact: String,
    val method: CatalogServerKnowledgeMappingMethod,
    val confidence: Double,
    val reason: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank"
        }

        require(serverKey.isNotBlank()) {
            "serverKey must not be blank"
        }

        require(sourceArtifact.isNotBlank()) {
            "sourceArtifact must not be blank"
        }

        require(confidence in 0.0..1.0) {
            "confidence must be between 0.0 and 1.0"
        }

        require(reason.isNotBlank()) {
            "reason must not be blank"
        }
    }
}