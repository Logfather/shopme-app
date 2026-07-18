package de.shopme.tools.knowledge.ai.builder.pollinator

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.pollinator.PollinatorKnowledge
import de.shopme.tools.knowledge.pollinator.PollinatorScore

class MergedCandidatePollinatorKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): PollinatorKnowledge {
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
                                        KnowledgeDimensionCandidateType.POLLINATOR
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val score =
                        toScore(payload)
                            ?: return@mapNotNull null

                    key to PollinatorScore(
                        score = score
                    )
                }
                .toMap()
                .toSortedMap()

        return PollinatorKnowledge(
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
                it >= 0.0
            }
    }
}