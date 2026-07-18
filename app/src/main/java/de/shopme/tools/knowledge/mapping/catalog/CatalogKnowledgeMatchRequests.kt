package de.shopme.tools.knowledge.mapping.catalog

data class CatalogKnowledgeMatchRequests(
    val version: Int,
    val requests: List<CatalogKnowledgeMatchRequest>
) {

    init {
        require(version > 0) {
            "version must be greater than zero"
        }

        requireNoDuplicateRequests()

        requireRequestsAreDeterministicallyOrdered()
    }


    private fun requireNoDuplicateRequests() {

        val duplicates =
            requests
                .groupingBy {
                    RequestIdentity(
                        catalogKey = it.catalogKey,
                        serverArtifact = it.serverArtifact
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "Duplicate catalog knowledge match requests: " +
                    duplicates.joinToString {
                        "${it.catalogKey} @ ${it.serverArtifact}"
                    }
        }
    }


    private fun requireRequestsAreDeterministicallyOrdered() {

        require(
            requests ==
                    requests.sortedWith(REQUEST_ORDER)
        ) {
            "requests must be ordered by " +
                    "serverArtifact and catalogKey"
        }
    }


    private data class RequestIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )


    companion object {

        val REQUEST_ORDER:
                Comparator<CatalogKnowledgeMatchRequest> =
            compareBy<CatalogKnowledgeMatchRequest> {
                it.serverArtifact
            }.thenBy {
                it.catalogKey
            }
    }
}