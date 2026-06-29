package de.shopme.tools.knowledge.off

class OFFKnowledgeImportCandidateBuilder {

    fun build(
        plan: OFFKnowledgeProposalApplyPlan
    ): List<OFFKnowledgeImportCandidate> {

        return plan.entries.map { entry ->

            OFFKnowledgeImportCandidate(
                catalogNormalizedName =
                    entry.catalogNormalizedName,

                dimension =
                    entry.dimension,

                source =
                    entry.source,

                reference =
                    entry.reference,

                offCode =
                    entry.offCode,

                offProductName =
                    entry.offProductName
            )
        }
    }
}