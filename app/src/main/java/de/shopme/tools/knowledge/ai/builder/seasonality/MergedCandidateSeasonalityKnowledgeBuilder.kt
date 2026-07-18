package de.shopme.tools.knowledge.ai.builder.seasonality

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.seasonality.SeasonalityKnowledge

class MergedCandidateSeasonalityKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): SeasonalityKnowledge {

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
                                        KnowledgeDimensionCandidateType.SEASONALITY
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val months =
                        toMonths(payload)
                            ?: return@mapNotNull null

                    key to months
                }
                .toMap()
                .toSortedMap()

        return SeasonalityKnowledge(
            entries = entries
        )
    }

    private fun toMonths(
        payload: Any?
    ): List<Int>? {

        val map =
            payload as? Map<*, *>
                ?: return null

        val value =
            map["months"]
                ?: return null

        val months =
            when (value) {

                is List<*> ->
                    value
                        .mapNotNull {
                            when (it) {
                                is Number -> it.toInt()
                                is String -> it.toIntOrNull()
                                else -> null
                            }
                        }

                else ->
                    return null
            }

        return months
            .filter {
                it in 1..12
            }
            .distinct()
            .sorted()
            .takeIf {
                it.isNotEmpty()
            }
    }
}