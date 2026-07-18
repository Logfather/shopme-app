package de.shopme.tools.knowledge.ai.builder.waterstress

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.waterstress.WaterStress
import de.shopme.tools.knowledge.waterstress.WaterStressKnowledge

class MergedCandidateWaterStressKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): WaterStressKnowledge {
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
                                        KnowledgeDimensionCandidateType.WATER_STRESS
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val score =
                        toScore(payload)
                            ?: return@mapNotNull null

                    key to WaterStress(
                        score = score
                    )
                }
                .toMap()
                .toSortedMap()

        return WaterStressKnowledge(
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
                it > 0.0
            }
    }
}