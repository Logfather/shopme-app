package de.shopme.tools.knowledge.rebuild.nutrition.adapter

data class RejectedStrongNutritionCandidateValidationReport(
    val version: Int,
    val candidateCount: Int,
    val acceptedCount: Int,
    val rejectedCount: Int,
    val entries:
    List<RejectedStrongNutritionCandidateValidationEntry>
) {

    init {
        require(version > 0) {
            "version must be greater than zero."
        }

        require(candidateCount >= 0) {
            "candidateCount must not be negative."
        }

        require(acceptedCount >= 0) {
            "acceptedCount must not be negative."
        }

        require(rejectedCount >= 0) {
            "rejectedCount must not be negative."
        }

        require(
            candidateCount ==
                    entries.size
        ) {
            "candidateCount must equal entries size."
        }

        require(
            acceptedCount +
                    rejectedCount ==
                    candidateCount
        ) {
            "acceptedCount and rejectedCount must cover all candidates."
        }

        require(
            acceptedCount ==
                    entries.count {
                        it.accepted
                    }
        ) {
            "acceptedCount differs from entries."
        }

        require(
            rejectedCount ==
                    entries.count {
                        !it.accepted
                    }
        ) {
            "rejectedCount differs from entries."
        }

        require(
            entries ==
                    entries.sortedBy {
                        normalizeKey(
                            value =
                                it.catalogKey
                        )
                    }
        ) {
            "entries must be sorted deterministically by catalogKey."
        }

        val duplicateCatalogKeys =
            entries
                .groupingBy {
                    normalizeKey(
                        value =
                            it.catalogKey
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Duplicate rejected strong nutrition validations: " +
                    duplicateCatalogKeys
                        .sorted()
                        .joinToString()
        }
    }

    companion object {

        const val CURRENT_VERSION =
            1

        private fun normalizeKey(
            value: String
        ): String =
            value
                .trim()
                .lowercase()
                .replace(
                    Regex("\\s+"),
                    " "
                )
    }
}