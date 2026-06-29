package de.shopme.tools.knowledge.off

class CarbonVariantCoverageCandidateBuilder(

    private val resolver: CarbonBaseFoodVariantResolver =
        CarbonBaseFoodVariantResolver()

) {

    fun build(
        catalogNormalizedNames: List<String>,
        coveredCarbonNames: Set<String>
    ): List<CarbonVariantCoverageCandidate> {

        return catalogNormalizedNames
            .distinct()
            .filterNot {
                it in coveredCarbonNames
            }
            .mapNotNull { name ->

                val resolved =
                    resolver.resolve(
                        normalizedName = name,
                        coveredCarbonNames = coveredCarbonNames
                    )
                        ?: return@mapNotNull null

                CarbonVariantCoverageCandidate(
                    catalogNormalizedName =
                        name,

                    resolvedCarbonReference =
                        resolved
                )
            }
            .sortedWith(
                compareBy(
                    { it.resolvedCarbonReference },
                    { it.catalogNormalizedName }
                )
            )
    }
}