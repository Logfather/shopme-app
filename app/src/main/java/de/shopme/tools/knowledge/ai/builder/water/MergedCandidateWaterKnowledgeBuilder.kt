package de.shopme.tools.knowledge.ai.builder.water

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.waterfootprint.WaterFootprint
import de.shopme.tools.knowledge.waterfootprint.WaterKnowledge

class MergedCandidateWaterKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): WaterKnowledge {
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
                                        KnowledgeDimensionCandidateType.WATER
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val litersPerKilogram =
                        toLitersPerKilogram(payload)
                            ?: return@mapNotNull null

                    key to WaterFootprint(
                        litersPerKilogram = litersPerKilogram
                    )
                }
                .toMap()
                .toSortedMap()

        return WaterKnowledge(
            entries = entries
        )
    }

    private fun toLitersPerKilogram(
        payload: Any?
    ): Double? {
        val map =
            payload as? Map<*, *>
                ?: return null

        return when (val value = map["litersPerKilogram"]) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
            ?.takeIf {
                it > 0.0
            }
    }
}