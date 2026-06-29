package de.shopme.tools.knowledge.off

class CarbonVariantCoverageMergeCandidateBuilder {

    fun build(
        variantCandidates: List<CarbonVariantCoverageCandidate>,
        carbonCandidates: List<OFFCarbonKnowledgeArtifactCandidate>
    ): List<CarbonVariantCoverageMergeCandidate> {

        val carbonByName =
            carbonCandidates
                .associateBy {
                    it.catalogNormalizedName
                }

        return variantCandidates
            .mapNotNull { variant ->

                val sourceCarbon =
                    carbonByName[
                        variant.resolvedCarbonReference
                    ]
                        ?: return@mapNotNull null

                CarbonVariantCoverageMergeCandidate(
                    catalogNormalizedName =
                        variant.catalogNormalizedName,

                    resolvedCarbonReference =
                        variant.resolvedCarbonReference,

                    kilogramsPerKilogram =
                        sourceCarbon.kilogramsCo2PerKilogram,

                    source =
                        variant.source
                )
            }
            .sortedBy {
                it.catalogNormalizedName
            }
    }
}