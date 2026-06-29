package de.shopme.tools.knowledge.off

class OFFKnowledgeProposalBuilder {

    fun build(
        candidates: List<OFFKnowledgeCandidate>
    ): List<OFFKnowledgeProposal> {

        return candidates
            .mapNotNull { candidate ->

                val reference =
                    candidate
                        .offCode
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let {
                            "off:$it"
                        }
                        ?: return@mapNotNull null

                val proposedReferences =
                    candidate
                        .dimensions
                        .associateWith {
                            reference
                        }

                OFFKnowledgeProposal(
                    catalogNormalizedName =
                        candidate.catalogNormalizedName,

                    offCode =
                        candidate.offCode,

                    offProductName =
                        candidate.offProductName,

                    source =
                        candidate.source,

                    proposedReferences =
                        proposedReferences,

                    dimensions =
                        candidate.dimensions
                )
            }
    }
}