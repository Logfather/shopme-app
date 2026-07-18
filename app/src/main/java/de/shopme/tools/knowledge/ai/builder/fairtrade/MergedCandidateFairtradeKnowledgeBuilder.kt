package de.shopme.tools.knowledge.ai.builder.fairtrade

import de.shopme.tools.knowledge.fairtrade.FairTrade
import de.shopme.tools.knowledge.fairtrade.FairTradeKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateFairtradeKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): FairTradeKnowledge {

        val entries =
            candidates
                .mapNotNull { candidate ->

                    val key =
                        candidate.canonicalId.trim()

                    if (key.isBlank()) {
                        return@mapNotNull null
                    }

                    val payload =
                        candidate.dimensions
                            .firstOrNull { dimension ->
                                dimension.dimension ==
                                        KnowledgeDimensionCandidateType.FAIRTRADE
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val fairTrade =
                        toFairTrade(payload)
                            ?: return@mapNotNull null

                    key to fairTrade
                }
                .toMap()
                .toSortedMap()

        return FairTradeKnowledge(
            entries = entries
        )
    }

    private fun toFairTrade(
        payload: Any?
    ): FairTrade? {

        val map =
            payload as? Map<*, *>
                ?: return null

        val score =
            when (
                val value =
                    map["score"]
                        ?: map["fairtradeScore"]
                        ?: map["value"]
            ) {

                is Number ->
                    value.toDouble()

                is String ->
                    value.toDoubleOrNull()

                else ->
                    null
            } ?: return null

        return FairTrade(
            score = score
        )
    }
}