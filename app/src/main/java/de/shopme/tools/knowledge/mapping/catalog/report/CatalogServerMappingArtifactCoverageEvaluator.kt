package de.shopme.tools.knowledge.mapping.catalog.report

class CatalogServerMappingArtifactCoverageEvaluator {

    fun evaluate(
        mappings: List<ReusableCatalogServerMapping>,
        serverKeysByArtifact: Map<String, Set<String>>
    ): CatalogServerMappingCoverageReport {

        val normalizedMappings =
            mappings
                .distinct()
                .sortedWith(
                    ReusableCatalogServerMapping.ORDER
                )

        val artifacts =
            serverKeysByArtifact
                .toSortedMap()
                .map { (artifact, serverKeys) ->

                    evaluateArtifact(
                        artifact = artifact,
                        mappings = normalizedMappings,
                        serverKeys = serverKeys
                    )
                }

        return CatalogServerMappingCoverageReport(
            version =
                CatalogServerMappingCoverageReport.CURRENT_VERSION,
            mappingCount =
                normalizedMappings.size,
            artifactCount =
                artifacts.size,
            artifacts =
                artifacts
        )
    }


    private fun evaluateArtifact(
        artifact: String,
        mappings: List<ReusableCatalogServerMapping>,
        serverKeys: Set<String>
    ): CatalogServerMappingArtifactCoverage {

        val reusableMappings =
            mappings.filter { mapping ->
                mapping.serverKey in serverKeys
            }

        val missingMappings =
            mappings
                .filterNot { mapping ->
                    mapping.serverKey in serverKeys
                }
                .map { mapping ->
                    CatalogServerMappingMiss(
                        catalogKey =
                            mapping.catalogKey,
                        serverKey =
                            mapping.serverKey
                    )
                }
                .sortedWith(
                    CatalogServerMappingMiss.ORDER
                )

        val reusableCatalogKeys =
            reusableMappings
                .map {
                    it.catalogKey
                }
                .distinct()
                .sorted()

        return CatalogServerMappingArtifactCoverage(
            artifact =
                artifact,
            serverKeyCount =
                serverKeys.size,
            mappingCount =
                mappings.size,
            reusableMappingCount =
                reusableMappings.size,
            missingMappingCount =
                missingMappings.size,
            reusableCatalogKeys =
                reusableCatalogKeys,
            missingMappings =
                missingMappings
        )
    }
}