package de.shopme.tools.knowledge.off

import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId

class OFFCarbonKnowledgeImportCandidateFilter {

    fun filter(
        candidates: List<OFFKnowledgeImportCandidate>
    ): List<OFFKnowledgeImportCandidate> {

        return candidates.filter {

            it.dimension ==
                    KnowledgeDimensionId.CARBON
        }
    }
}