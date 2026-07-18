package de.shopme.tools.knowledge.ai.builder.pesticides

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.pesticides.Pesticide
import de.shopme.tools.knowledge.pesticides.PesticideKnowledge

class MergedCandidatePesticidesKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): PesticideKnowledge {
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
                                        KnowledgeDimensionCandidateType.PESTICIDES
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val score =
                        toScore(payload)
                            ?: return@mapNotNull null

                    key to Pesticide(
                        score = score
                    )
                }
                .toMap()
                .toSortedMap()

        return PesticideKnowledge(
            entries = entries
        )
    }

    private fun toScore(
        payload: Any?
    ): Double? {
        val map =
            payload as? Map<*, *>
                ?: return null

        return when (val value = map["score"]) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
            ?.takeIf {
                it in 0.0..1.0
            }
    }
}