package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.openfoodfacts.OFFKnowledgeProposalApplyEntry

class OFFKnowledgeProposalApplyPlanBuilder {

    fun build(
        proposals: List<OFFKnowledgeProposal>
    ): OFFKnowledgeProposalApplyPlan {

        val entries =
            proposals.flatMap { proposal ->

                proposal.proposedReferences.map { entry ->

                    OFFKnowledgeProposalApplyEntry(
                        catalogNormalizedName =
                            proposal.catalogNormalizedName,

                        dimension =
                            entry.key,

                        reference =
                            entry.value,

                        source =
                            proposal.source,

                        offCode =
                            proposal.offCode,

                        offProductName =
                            proposal.offProductName
                    )
                }
            }.sortedWith(
                compareBy(
                    { it.catalogNormalizedName },
                    { it.dimension.name }
                )
            )

        return OFFKnowledgeProposalApplyPlan(
            entries = entries
        )
    }
}