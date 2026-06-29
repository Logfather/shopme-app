package de.shopme.tools.knowledge.carbon.merge

import de.shopme.tools.knowledge.carbon.model.CarbonKnowledgeCandidate
import de.shopme.tools.knowledge.source.KnowledgeMerger
import de.shopme.tools.knowledge.source.KnowledgeSourceId

class CarbonCandidateMerger :

    KnowledgeMerger<
            CarbonKnowledgeCandidate
            > {

    override fun merge(
        candidates: List<CarbonKnowledgeCandidate>
    ): Map<String, CarbonKnowledgeCandidate> {

        return candidates

            .groupBy {

                it.reference.lowercase()

            }

            .mapValues { (_, entries) ->

                entries.firstOrNull {

                    it.source ==
                            KnowledgeSourceId.AGRIBALYSE

                }

                    ?: entries.first()

            }
    }
}