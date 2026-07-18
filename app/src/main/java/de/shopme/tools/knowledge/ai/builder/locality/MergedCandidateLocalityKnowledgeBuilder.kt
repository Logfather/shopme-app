package de.shopme.tools.knowledge.ai.builder.locality

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.locality.Locality
import de.shopme.tools.knowledge.locality.LocalityKnowledge

class MergedCandidateLocalityKnowledgeBuilder {

    fun build(
        candidates: List<CanonicalKnowledgeCandidate>
    ) =
        build(
            candidates.asSequence()
        )

    fun build(
        candidates: Sequence<CanonicalKnowledgeCandidate>
    ): LocalityKnowledge {
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
                                        KnowledgeDimensionCandidateType.LOCALITY
                            }
                            ?.payload
                            ?: return@mapNotNull null

                    val locality =
                        toLocality(payload)
                            ?: return@mapNotNull null

                    key to locality
                }
                .toMap()
                .toSortedMap()

        return LocalityKnowledge(
            entries = entries
        )
    }

    private fun toLocality(
        payload: Any?
    ): Locality? {
        val map =
            payload as? Map<*, *>
                ?: return null

        return when (val value = map["locality"]) {
            is Locality -> value
            is String -> value.toLocality()
            else -> null
        }
    }

    private fun String.toLocality(): Locality? =
        runCatching {
            Locality.valueOf(
                trim()
                    .uppercase()
                    .replace("-", "_")
                    .replace(" ", "_")
            )
        }
            .getOrNull()
}