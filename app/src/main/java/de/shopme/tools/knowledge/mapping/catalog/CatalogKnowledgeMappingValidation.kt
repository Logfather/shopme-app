package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMappingValidation(
    val catalogKey: String,
    val serverArtifact: String,
    val selectedServerKey: String?,
    val confidence: Double,
    val status: CatalogKnowledgeMappingValidationStatus,
    val reason: String
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank"
        }

        require(serverArtifact.isNotBlank()) {
            "serverArtifact must not be blank"
        }

        require(confidence in 0.0..1.0) {
            "confidence must be between 0.0 and 1.0"
        }

        require(reason.isNotBlank()) {
            "reason must not be blank"
        }

        if (
            status ==
            CatalogKnowledgeMappingValidationStatus.ACCEPTED
        ) {
            require(
                !selectedServerKey.isNullOrBlank()
            ) {
                "ACCEPTED validation requires selectedServerKey"
            }
        }
    }


    val isAccepted: Boolean
        get() =
            status ==
                    CatalogKnowledgeMappingValidationStatus.ACCEPTED
}