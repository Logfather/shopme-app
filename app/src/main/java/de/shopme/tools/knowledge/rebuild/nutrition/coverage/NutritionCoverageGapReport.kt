package de.shopme.tools.knowledge.rebuild.nutrition.coverage

data class NutritionCoverageGapReport(
    val version: Int,
    val catalogItemCount: Int,
    val coveredCatalogItemCount: Int,
    val missingCatalogItemCount: Int,
    val classifiedGapCount: Int,
    val unclassifiedGapCount: Int,
    val countsByType: Map<String, Int>,
    val gaps: List<NutritionCoverageGap>
) {

    init {
        require(version > 0) {
            "version must be greater than zero."
        }

        require(catalogItemCount >= 0) {
            "catalogItemCount must not be negative."
        }

        require(coveredCatalogItemCount >= 0) {
            "coveredCatalogItemCount must not be negative."
        }

        require(missingCatalogItemCount >= 0) {
            "missingCatalogItemCount must not be negative."
        }

        require(classifiedGapCount >= 0) {
            "classifiedGapCount must not be negative."
        }

        require(unclassifiedGapCount >= 0) {
            "unclassifiedGapCount must not be negative."
        }

        require(
            coveredCatalogItemCount +
                    missingCatalogItemCount ==
                    catalogItemCount
        ) {
            "Covered and missing catalog items must equal the catalog " +
                    "item count: covered=$coveredCatalogItemCount, " +
                    "missing=$missingCatalogItemCount, " +
                    "catalog=$catalogItemCount."
        }

        require(
            gaps.size ==
                    missingCatalogItemCount
        ) {
            "Gap count must equal missing catalog item count: " +
                    "gaps=${gaps.size}, missing=$missingCatalogItemCount."
        }

        require(
            classifiedGapCount +
                    unclassifiedGapCount ==
                    missingCatalogItemCount
        ) {
            "Classified and unclassified gap counts must equal the " +
                    "missing catalog item count."
        }

        require(
            classifiedGapCount ==
                    gaps.count {
                        it.type !=
                                NutritionCoverageGapType.UNKNOWN
                    }
        ) {
            "classifiedGapCount differs from classified gaps."
        }

        require(
            unclassifiedGapCount ==
                    gaps.count {
                        it.type ==
                                NutritionCoverageGapType.UNKNOWN
                    }
        ) {
            "unclassifiedGapCount differs from UNKNOWN gaps."
        }

        require(
            gaps ==
                    gaps.sortedBy {
                        it.catalogKey
                    }
        ) {
            "Coverage gaps must be ordered by catalogKey."
        }

        val duplicateCatalogKeys =
            gaps
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Coverage gap report contains duplicate catalog keys: " +
                    duplicateCatalogKeys
                        .sorted()
                        .joinToString()
        }

        require(
            countsByType.keys ==
                    countsByType.keys.sorted().toSet()
        ) {
            "countsByType keys must be deterministically ordered."
        }

        require(
            countsByType.values.all {
                it >= 0
            }
        ) {
            "countsByType must not contain negative counts."
        }

        require(
            countsByType.values.sum() ==
                    gaps.size
        ) {
            "countsByType total must equal the number of gaps."
        }

        val actualCounts =
            gaps
                .groupingBy {
                    it.type.name
                }
                .eachCount()
                .toSortedMap()

        require(
            countsByType ==
                    actualCounts
        ) {
            "countsByType differs from the actual gap distribution."
        }
    }

    companion object {

        const val CURRENT_VERSION =
            1
    }
}