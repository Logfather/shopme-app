package de.shopme.tools.knowledge.ai.builder.processing

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.processing.ProcessingKnowledge
import de.shopme.tools.knowledge.processing.ProcessingLevel

class MergedCandidateProcessingKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): ProcessingKnowledge {
        val entries: Map<String, ProcessingLevel> =
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
                                        KnowledgeDimensionCandidateType.PROCESSING
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val level =
                        toProcessingLevel(payload)
                            ?: return@mapNotNull null

                    key to level
                }
                .toMap()
                .toSortedMap()

        return ProcessingKnowledge(
            entries = entries
        )
    }

    private fun toProcessingLevel(
        payload: Any?
    ): ProcessingLevel? {
        val map =
            payload as? Map<*, *>
                ?: return null

        val novaGroup =
            when (val value = map["novaGroup"]) {
                is Number -> value.toInt()
                is String -> value.toIntOrNull()
                else -> null
            } ?: return null

        return novaGroup.toProcessingLevel()
    }

    private fun Int.toProcessingLevel(): ProcessingLevel? =
        when (this) {
            1 -> ProcessingLevel.NOVA_1
            2 -> ProcessingLevel.NOVA_2
            3 -> ProcessingLevel.NOVA_3
            4 -> ProcessingLevel.NOVA_4
            else -> null
        }
}