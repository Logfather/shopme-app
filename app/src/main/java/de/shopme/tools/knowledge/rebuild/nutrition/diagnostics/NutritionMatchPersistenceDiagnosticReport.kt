package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

data class NutritionMatchPersistenceDiagnosticReport(
    val version: Int,
    val matchDecisionCount: Int,
    val validationRecordCount: Int,
    val explicitlyAcceptedValidationCount: Int,
    val explicitlyRejectedValidationCount: Int,
    val mappingPresentCount: Int,
    val runtimePresentCount: Int,
    val fullyPersistedCount: Int,
    val missingPersistenceCount: Int,
    val countsByFirstMissingStage: Map<String, Int>,
    val expectedMatchNotPersistedCount: Int?,
    val diagnostics: List<NutritionMatchPersistenceDiagnostic>
) {

    init {
        require(version > 0) {
            "version must be greater than zero."
        }

        require(matchDecisionCount >= 0) {
            "matchDecisionCount must not be negative."
        }

        require(validationRecordCount >= 0) {
            "validationRecordCount must not be negative."
        }

        require(explicitlyAcceptedValidationCount >= 0) {
            "explicitlyAcceptedValidationCount must not be negative."
        }

        require(explicitlyRejectedValidationCount >= 0) {
            "explicitlyRejectedValidationCount must not be negative."
        }

        require(mappingPresentCount >= 0) {
            "mappingPresentCount must not be negative."
        }

        require(runtimePresentCount >= 0) {
            "runtimePresentCount must not be negative."
        }

        require(fullyPersistedCount >= 0) {
            "fullyPersistedCount must not be negative."
        }

        require(missingPersistenceCount >= 0) {
            "missingPersistenceCount must not be negative."
        }

        require(
            expectedMatchNotPersistedCount == null ||
                    expectedMatchNotPersistedCount >= 0
        ) {
            "expectedMatchNotPersistedCount must be null or " +
                    "non-negative."
        }

        require(
            diagnostics.size ==
                    matchDecisionCount
        ) {
            "Diagnostic count must equal MATCH decision count: " +
                    "diagnostics=${diagnostics.size}, " +
                    "matches=$matchDecisionCount."
        }

        require(
            validationRecordCount ==
                    diagnostics.count {
                        it.validationRecordPresent
                    }
        ) {
            "validationRecordCount differs from diagnostics."
        }

        require(
            explicitlyAcceptedValidationCount ==
                    diagnostics.count {
                        it.validationAccepted == true
                    }
        ) {
            "explicitlyAcceptedValidationCount differs from " +
                    "diagnostics."
        }

        require(
            explicitlyRejectedValidationCount ==
                    diagnostics.count {
                        it.validationAccepted == false
                    }
        ) {
            "explicitlyRejectedValidationCount differs from " +
                    "diagnostics."
        }

        require(
            mappingPresentCount ==
                    diagnostics.count {
                        it.mappingPresent
                    }
        ) {
            "mappingPresentCount differs from diagnostics."
        }

        require(
            runtimePresentCount ==
                    diagnostics.count {
                        it.runtimePresent
                    }
        ) {
            "runtimePresentCount differs from diagnostics."
        }

        require(
            fullyPersistedCount ==
                    diagnostics.count {
                        it.fullyPersisted
                    }
        ) {
            "fullyPersistedCount differs from diagnostics."
        }

        require(
            missingPersistenceCount ==
                    diagnostics.count {
                        !it.fullyPersisted
                    }
        ) {
            "missingPersistenceCount differs from diagnostics."
        }

        require(
            fullyPersistedCount +
                    missingPersistenceCount ==
                    matchDecisionCount
        ) {
            "Fully persisted and missing decisions must cover all " +
                    "MATCH decisions."
        }

        require(
            diagnostics ==
                    diagnostics.sortedBy {
                        it.catalogKey
                    }
        ) {
            "Diagnostics must be ordered by catalogKey."
        }

        val duplicateCatalogKeys =
            diagnostics
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Diagnostics contain duplicate catalog keys: " +
                    duplicateCatalogKeys
                        .sorted()
                        .joinToString()
        }

        val actualCountsByStage =
            diagnostics
                .groupingBy {
                    it.firstMissingStage.name
                }
                .eachCount()
                .toSortedMap()

        require(
            countsByFirstMissingStage ==
                    actualCountsByStage
        ) {
            "countsByFirstMissingStage differs from diagnostics."
        }

        if (expectedMatchNotPersistedCount != null) {
            require(
                missingPersistenceCount ==
                        expectedMatchNotPersistedCount
            ) {
                "Missing MATCH persistence count differs from the " +
                        "coverage-gap report: diagnosed=" +
                        "$missingPersistenceCount, expected=" +
                        "$expectedMatchNotPersistedCount."
            }
        }
    }

    companion object {

        const val CURRENT_VERSION =
            1
    }
}