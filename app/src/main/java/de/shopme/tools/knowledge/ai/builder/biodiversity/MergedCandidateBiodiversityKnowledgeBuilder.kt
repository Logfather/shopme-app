package de.shopme.tools.knowledge.ai.builder.biodiversity

import de.shopme.tools.knowledge.biodiversity.BiodiversityKnowledge
import de.shopme.tools.knowledge.biodiversity.BiodiversityScore
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateBiodiversityKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): BiodiversityKnowledge {
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
                                        KnowledgeDimensionCandidateType.BIODIVERSITY
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val score =
                        toScore(payload)
                            ?: return@mapNotNull null

                    key to BiodiversityScore(
                        score = score
                    )
                }
                .toMap()
                .toSortedMap()

        return BiodiversityKnowledge(
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