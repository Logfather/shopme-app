package de.shopme.tools.knowledge.off

class OFFCarbonKnowledgeImportCandidateBuilder {

    fun build(
        candidates: List<OFFKnowledgeImportCandidate>
    ): List<OFFCarbonKnowledgeImportCandidate> {

        return OFFCarbonKnowledgeImportCandidateFilter()
            .filter(candidates)
            .map { candidate ->

                OFFCarbonKnowledgeImportCandidate(

                    catalogNormalizedName =
                        candidate.catalogNormalizedName,

                    source =
                        candidate.source,

                    reference =
                        candidate.reference,

                    offCode =
                        candidate.offCode,

                    offProductName =
                        candidate.offProductName
                )
            }
    }
}