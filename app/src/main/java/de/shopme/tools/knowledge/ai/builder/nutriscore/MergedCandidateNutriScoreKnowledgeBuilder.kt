package de.shopme.tools.knowledge.ai.builder.nutriscore

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.nutriscore.NutriScore
import de.shopme.tools.knowledge.nutriscore.NutriScoreFactsKnowledge

class MergedCandidateNutriScoreKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): NutriScoreFactsKnowledge {

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
                                        KnowledgeDimensionCandidateType.NUTRI_SCORE
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val score =
                        toNutriScore(payload)
                            ?: return@mapNotNull null

                    key to score
                }
                .toMap()
                .toSortedMap()

        return NutriScoreFactsKnowledge(
            entries
        )
    }

    private fun toNutriScore(
        payload: Any?
    ): NutriScore? {

        val map =
            payload as? Map<*, *>
                ?: return null

        return when (val value = map["score"]) {

            is NutriScore ->
                value

            is String ->
                runCatching {
                    NutriScore.valueOf(
                        value
                            .trim()
                            .uppercase()
                    )
                }.getOrNull()

            else ->
                null
        }
    }
}