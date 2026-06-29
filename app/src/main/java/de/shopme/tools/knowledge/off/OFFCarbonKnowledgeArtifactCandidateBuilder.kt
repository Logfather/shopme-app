package de.shopme.tools.knowledge.off

class OFFCarbonKnowledgeArtifactCandidateBuilder {

    fun build(
        carbonCandidates: List<OFFCarbonKnowledgeImportCandidate>,
        extracts: List<OFFHivraExtract>
    ): List<OFFCarbonKnowledgeArtifactCandidate> {

        val extractsByCode =
            extracts
                .mapNotNull { extract ->

                    val code =
                        extract.code
                            ?.takeIf {
                                it.isNotBlank()
                            }
                            ?: return@mapNotNull null

                    code to extract
                }
                .toMap()

        return carbonCandidates.mapNotNull { candidate ->

            val code =
                candidate.reference
                    .removePrefix("off:")
                    .takeIf {
                        it.isNotBlank()
                    }
                    ?: candidate.offCode
                        ?.takeIf {
                            it.isNotBlank()
                        }
                    ?: return@mapNotNull null

            val extract =
                extractsByCode[code]
                    ?: return@mapNotNull null

            val co2 =
                extract
                    .carbon
                    ?.co2Total
                    ?.takeIf {
                        it > 0.0
                    }
                    ?: return@mapNotNull null

            OFFCarbonKnowledgeArtifactCandidate(
                catalogNormalizedName =
                    candidate.catalogNormalizedName,

                kilogramsCo2PerKilogram =
                    co2,

                source =
                    candidate.source,

                reference =
                    candidate.reference
            )
        }
    }
}