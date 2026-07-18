package de.shopme.tools.knowledge.mapping.catalog.report

data class CatalogServerMappingCoverageReport(
    val version: Int,
    val mappingCount: Int,
    val artifactCount: Int,
    val artifacts: List<CatalogServerMappingArtifactCoverage>
) {

    init {
        require(version > 0) {
            "version must be greater than zero"
        }

        require(mappingCount >= 0) {
            "mappingCount must not be negative"
        }

        require(artifactCount >= 0) {
            "artifactCount must not be negative"
        }

        require(
            artifactCount ==
                    artifacts.size
        ) {
            "artifactCount must equal artifacts.size"
        }

        require(
            artifacts ==
                    artifacts.sortedBy {
                        it.artifact
                    }
        ) {
            "artifacts must be sorted by artifact"
        }

        require(
            artifacts.map {
                it.artifact
            }.distinct().size ==
                    artifacts.size
        ) {
            "artifacts must not contain duplicates"
        }
    }


    companion object {

        const val CURRENT_VERSION =
            1
    }
}