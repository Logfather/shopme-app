package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMatchDecision(
    val catalogKey: String,
    val serverArtifact: String,
    val type: CatalogKnowledgeMatchDecisionType,
    val selectedServerKey: String?,
    val confidence: Double,
    val reason: String,
    val decisionSource:
    CatalogKnowledgeMatchDecisionSource =
        CatalogKnowledgeMatchDecisionSource.CHAT_GPT
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

        requireSelectedServerKeyMatchesDecisionType()
    }


    val isMatch: Boolean
        get() =
            type ==
                    CatalogKnowledgeMatchDecisionType.MATCH


    private fun requireSelectedServerKeyMatchesDecisionType() {

        when (type) {

            CatalogKnowledgeMatchDecisionType.MATCH ->
                require(
                    !selectedServerKey.isNullOrBlank()
                ) {
                    "MATCH decision requires selectedServerKey"
                }

            CatalogKnowledgeMatchDecisionType.NO_MATCH ->
                require(
                    selectedServerKey == null
                ) {
                    "NO_MATCH decision must not contain selectedServerKey"
                }
        }
    }
}