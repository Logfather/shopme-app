package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMappingValidationReport(
    val minimumConfidence: Double,
    val validations: List<CatalogKnowledgeMappingValidation>
) {

    init {
        require(minimumConfidence in 0.0..1.0) {
            "minimumConfidence must be between 0.0 and 1.0"
        }

        require(
            validations ==
                    validations.sortedWith(VALIDATION_ORDER)
        ) {
            "validations must be ordered by serverArtifact and catalogKey"
        }
    }


    val acceptedCount: Int
        get() =
            validations.count {
                it.isAccepted
            }


    val rejectedCount: Int
        get() =
            validations.size -
                    acceptedCount


    companion object {

        val VALIDATION_ORDER:
                Comparator<CatalogKnowledgeMappingValidation> =
            compareBy<CatalogKnowledgeMappingValidation> {
                it.serverArtifact
            }.thenBy {
                it.catalogKey
            }
    }
}