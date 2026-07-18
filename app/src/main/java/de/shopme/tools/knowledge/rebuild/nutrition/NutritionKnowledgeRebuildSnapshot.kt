package de.shopme.tools.knowledge.rebuild.nutrition

data class NutritionKnowledgeRebuildSnapshot(
    val mappingCount: Int,
    val catalogItemCount: Int,
    val exactMatchCount: Int,
    val mappedMatchCount: Int,
    val runtimeEntryCount: Int,
    val coveredCatalogItemCount: Int,
    val missingCatalogItemCount: Int,
    val coverage: Double
) {

    init {
        require(mappingCount >= 0) {
            "mappingCount must not be negative."
        }

        require(catalogItemCount >= 0) {
            "catalogItemCount must not be negative."
        }

        require(exactMatchCount >= 0) {
            "exactMatchCount must not be negative."
        }

        require(mappedMatchCount >= 0) {
            "mappedMatchCount must not be negative."
        }

        require(runtimeEntryCount >= 0) {
            "runtimeEntryCount must not be negative."
        }

        require(coveredCatalogItemCount >= 0) {
            "coveredCatalogItemCount must not be negative."
        }

        require(missingCatalogItemCount >= 0) {
            "missingCatalogItemCount must not be negative."
        }

        require(
            exactMatchCount +
                    mappedMatchCount ==
                    coveredCatalogItemCount
        ) {
            "Exact and mapped matches must equal covered " +
                    "catalog items: exact=$exactMatchCount, " +
                    "mapped=$mappedMatchCount, " +
                    "covered=$coveredCatalogItemCount."
        }

        require(
            runtimeEntryCount ==
                    coveredCatalogItemCount
        ) {
            "Runtime entry count must equal covered catalog " +
                    "item count: runtime=$runtimeEntryCount, " +
                    "covered=$coveredCatalogItemCount."
        }

        require(
            coveredCatalogItemCount +
                    missingCatalogItemCount ==
                    catalogItemCount
        ) {
            "Covered and missing catalog items must equal " +
                    "catalogItemCount: covered=" +
                    "$coveredCatalogItemCount, missing=" +
                    "$missingCatalogItemCount, catalog=" +
                    "$catalogItemCount."
        }

        require(coverage in 0.0..1.0) {
            "Coverage must be between 0.0 and 1.0."
        }

        val expectedCoverage =
            if (catalogItemCount == 0) {
                0.0
            } else {
                coveredCatalogItemCount.toDouble() /
                        catalogItemCount.toDouble()
            }

        require(
            kotlin.math.abs(
                coverage -
                        expectedCoverage
            ) <= COVERAGE_EPSILON
        ) {
            "Coverage differs from covered/catalog ratio: " +
                    "coverage=$coverage, expected=$expectedCoverage."
        }
    }

    private companion object {

        const val COVERAGE_EPSILON =
            1e-12
    }
}