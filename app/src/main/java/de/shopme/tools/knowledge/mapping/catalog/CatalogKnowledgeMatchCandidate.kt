package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMatchCandidate(
    val serverKey: String,
    val diagnosticScore: Double,
    val sharedTokens: List<String>
) {

    init {
        require(serverKey.isNotBlank()) {
            "serverKey must not be blank"
        }

        require(diagnosticScore in 0.0..1.0) {
            "diagnosticScore must be between 0.0 and 1.0"
        }

        require(
            sharedTokens.none {
                it.isBlank()
            }
        ) {
            "sharedTokens must not contain blank values"
        }

        require(
            sharedTokens.distinct().size ==
                    sharedTokens.size
        ) {
            "sharedTokens must not contain duplicates"
        }
    }
}