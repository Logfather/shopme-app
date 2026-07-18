package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMatchDecisions(
    val version: Int,
    val decisions: List<CatalogKnowledgeMatchDecision>
) {

    init {
        require(version > 0) {
            "version must be greater than zero"
        }

        requireNoDuplicateDecisions()
        requireDecisionsAreDeterministicallyOrdered()
    }


    private fun requireNoDuplicateDecisions() {

        val duplicateIdentities =
            decisions
                .groupingBy { decision ->
                    DecisionIdentity(
                        catalogKey =
                            decision.catalogKey,
                        serverArtifact =
                            decision.serverArtifact
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateIdentities.isEmpty()) {
            "Duplicate catalog knowledge match decisions: " +
                    duplicateIdentities
                        .sortedWith(
                            compareBy<DecisionIdentity> {
                                it.serverArtifact
                            }.thenBy {
                                it.catalogKey
                            }
                        )
                        .joinToString {
                            "${it.catalogKey} @ ${it.serverArtifact}"
                        }
        }
    }


    private fun requireDecisionsAreDeterministicallyOrdered() {

        require(
            decisions ==
                    decisions.sortedWith(DECISION_ORDER)
        ) {
            "decisions must be ordered by " +
                    "serverArtifact and catalogKey"
        }
    }


    private data class DecisionIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )


    companion object {

        val DECISION_ORDER:
                Comparator<CatalogKnowledgeMatchDecision> =
            compareBy<CatalogKnowledgeMatchDecision> {
                it.serverArtifact
            }.thenBy {
                it.catalogKey
            }
    }
}