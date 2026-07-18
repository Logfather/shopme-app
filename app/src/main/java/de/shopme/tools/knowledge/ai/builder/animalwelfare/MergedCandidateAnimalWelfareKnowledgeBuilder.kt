package de.shopme.tools.knowledge.ai.builder.animalwelfare

import de.shopme.tools.knowledge.animalwelfare.AnimalWelfare
import de.shopme.tools.knowledge.animalwelfare.AnimalWelfareKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateAnimalWelfareKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): AnimalWelfareKnowledge {

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
                                        KnowledgeDimensionCandidateType.ANIMAL_WELFARE
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val animalWelfare =
                        toAnimalWelfare(payload)
                            ?: return@mapNotNull null

                    key to animalWelfare
                }
                .toMap()
                .toSortedMap()

        return AnimalWelfareKnowledge(
            entries = entries
        )
    }

    private fun toAnimalWelfare(
        payload: Any?
    ): AnimalWelfare? {

        val map =
            payload as? Map<*, *>
                ?: return null

        val score =
            when (
                val value =
                    map["score"]
                        ?: map["animalWelfareScore"]
                        ?: map["value"]
            ) {
                is Number -> value.toDouble()
                is String -> value.toDoubleOrNull()
                else -> null
            } ?: return null

        return AnimalWelfare(
            score = score
        )
    }
}