package de.shopme.tools.knowledge.carbon.merge

import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeMergeReportBuilder

class CarbonCandidateMergeReportBuilder :

    KnowledgeMergeReportBuilder<
            CarbonKnowledgeCandidate
            > {

    override fun build(

        candidates: List<CarbonKnowledgeCandidate>,

        merged: Map<String, CarbonKnowledgeCandidate>

    ): CarbonCandidateMergeReport {

        val grouped =
            candidates.groupBy {
                it.reference.lowercase()
            }

        val conflicts =
            grouped
                .filter { (_, entries) ->
                    entries.map { it.kgCo2ePerKg }
                        .distinct()
                        .size > 1
                }
                .map { (reference, entries) ->
                    CarbonMergeConflict(
                        reference = reference,
                        candidates = entries
                    )
                }
                .sortedBy {
                    it.reference
                }

        return CarbonCandidateMergeReport(
            totalCandidates = candidates.size,
            mergedReferences = merged.size,
            conflicts = conflicts
        )
    }
}