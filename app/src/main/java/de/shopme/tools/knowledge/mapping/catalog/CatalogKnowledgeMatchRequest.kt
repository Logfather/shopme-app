package de.shopme.tools.knowledge.mapping.catalog



data class CatalogKnowledgeMatchRequest(
    val catalogKey: String,
    val serverArtifact: String,
    val candidates: List<CatalogKnowledgeMatchCandidate>
) {

    init {
        require(catalogKey.isNotBlank()) {
            "catalogKey must not be blank"
        }

        require(serverArtifact.isNotBlank()) {
            "serverArtifact must not be blank"
        }

        require(candidates.isNotEmpty()) {
            "candidates must not be empty"
        }

        requireNoDuplicateCandidates()
        requireCandidatesAreDeterministicallyOrdered()
    }


    private fun requireNoDuplicateCandidates() {

        val duplicateServerKeys =
            candidates
                .groupingBy {
                    it.serverKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateServerKeys.isEmpty()) {
            "Duplicate match candidates: " +
                    duplicateServerKeys
                        .sorted()
                        .joinToString()
        }
    }


    private fun requireCandidatesAreDeterministicallyOrdered() {

        require(
            candidates ==
                    candidates.sortedWith(CANDIDATE_ORDER)
        ) {
            "candidates must be ordered by " +
                    "diagnosticScore descending and serverKey ascending"
        }
    }


    companion object {

        val CANDIDATE_ORDER:
                Comparator<CatalogKnowledgeMatchCandidate> =
            compareByDescending<CatalogKnowledgeMatchCandidate> {
                it.diagnosticScore
            }.thenBy {
                it.serverKey
            }
    }
}