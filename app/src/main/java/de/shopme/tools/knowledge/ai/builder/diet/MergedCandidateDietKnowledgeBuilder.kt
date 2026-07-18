package de.shopme.tools.knowledge.ai.builder.diet

import de.shopme.tools.knowledge.diet.DietClassification
import de.shopme.tools.knowledge.diet.DietKnowledge
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class MergedCandidateDietKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): DietKnowledge {

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
                                        KnowledgeDimensionCandidateType.DIET
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val classifications =
                        toClassifications(payload)
                            ?: return@mapNotNull null

                    key to classifications
                }
                .toMap()
                .toSortedMap()

        return DietKnowledge(
            entries = entries
        )
    }


    private fun toClassifications(
        payload: Any?
    ): Set<DietClassification>? {

        val map =
            payload as? Map<*, *>
                ?: return null

        val values =
            map["classifications"]
                ?: return null

        return when (values) {

            is Collection<*> ->
                values
                    .mapNotNull {
                        when (it) {

                            is DietClassification ->
                                it

                            is String ->
                                runCatching {
                                    DietClassification.valueOf(
                                        it
                                            .trim()
                                            .uppercase()
                                    )
                                }.getOrNull()

                            else ->
                                null
                        }
                    }

            else ->
                emptyList()
        }
            .toSet()
            .takeIf {
                it.isNotEmpty()
            }
    }
}