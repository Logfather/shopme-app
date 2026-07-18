package de.shopme.tools.knowledge.mapping.catalog.report

import kotlin.collections.sortedWith

data class CatalogServerMappingArtifactCoverage(
    val artifact: String,
    val serverKeyCount: Int,
    val mappingCount: Int,
    val reusableMappingCount: Int,
    val missingMappingCount: Int,
    val reusableCatalogKeys: List<String>,
    val missingMappings: List<CatalogServerMappingMiss>
) {

    init {
        require(artifact.isNotBlank()) {
            "artifact must not be blank"
        }

        require(serverKeyCount >= 0) {
            "serverKeyCount must not be negative"
        }

        require(mappingCount >= 0) {
            "mappingCount must not be negative"
        }

        require(reusableMappingCount >= 0) {
            "reusableMappingCount must not be negative"
        }

        require(missingMappingCount >= 0) {
            "missingMappingCount must not be negative"
        }

        require(
            reusableMappingCount +
                    missingMappingCount ==
                    mappingCount
        ) {
            "reusableMappingCount + missingMappingCount " +
                    "must equal mappingCount"
        }

        require(
            reusableCatalogKeys ==
                    reusableCatalogKeys.sorted()
        ) {
            "reusableCatalogKeys must be sorted"
        }

        require(
            reusableCatalogKeys.distinct().size ==
                    reusableCatalogKeys.size
        ) {
            "reusableCatalogKeys must not contain duplicates"
        }

        require(
            missingMappings ==
                    missingMappings.sortedWith(
                        CatalogServerMappingMiss.ORDER
                    )
        ) {
            "missingMappings must be deterministically ordered"
        }
    }


    val coveragePercent: Double
        get() =
            if (mappingCount == 0) {
                0.0
            } else {
                reusableMappingCount
                    .toDouble()
                    .div(mappingCount)
                    .times(100.0)
            }
}