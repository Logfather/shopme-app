package de.shopme.tools.knowledge.ai.builder.foodmiles

import de.shopme.tools.knowledge.foodmiles.FoodMiles
import de.shopme.tools.knowledge.foodmiles.FoodMilesKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateFoodMilesKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): FoodMilesKnowledge {

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
                                        KnowledgeDimensionCandidateType.FOOD_MILES
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val kilometers =
                        toKilometers(payload)
                            ?: return@mapNotNull null

                    key to FoodMiles(
                        kilometers = kilometers
                    )
                }
                .toMap()
                .toSortedMap()

        return FoodMilesKnowledge(
            entries = entries
        )
    }

    private fun toKilometers(
        payload: Any?
    ): Double? {
        val map =
            payload as? Map<*, *>
                ?: return null

        return when (val value = map["kilometers"]) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
            ?.takeIf {
                it >= 0.0
            }
    }
}