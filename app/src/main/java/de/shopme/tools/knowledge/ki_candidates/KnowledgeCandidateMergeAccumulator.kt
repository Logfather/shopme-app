package de.shopme.tools.knowledge.ki_candidates

class KnowledgeCandidateMergeAccumulator {

    private val candidatesByKey =
        linkedMapOf<String, CanonicalKnowledgeCandidate>()

    private var conflicts =
        0

    private fun compactPayload(
        payload: Any
    ): Any =
        payload

    private fun compact(
        candidate: CanonicalKnowledgeCandidate
    ): CanonicalKnowledgeCandidate =
        candidate.copy(
            aliases =
                candidate.aliases
                    .take(1)
                    .toSet(),

            matchAliases =
                emptySet(),

            metadata =
                candidate.metadata.copy(
                    attributes =
                        emptyMap()
                )
        )

    fun candidates():
            Sequence<CanonicalKnowledgeCandidate> =
        candidatesByKey
            .values
            .asSequence()

    fun add(
        candidates: List<CanonicalKnowledgeCandidate>
    ) {
        candidates.forEach { candidate ->

            val compactCandidate =
                compact(candidate)

            val key =
                mergeKey(compactCandidate)

            val existing =
                candidatesByKey[key]

            if (existing == null) {
                candidatesByKey[key] =
                    compactCandidate
            } else {
                candidatesByKey[key] =
                    compact(
                        mergeSameKey(
                            existing = existing,
                            incoming = compactCandidate
                        )
                    )

                conflicts++
            }
        }
    }

    private fun mergeSameKey(
        existing: CanonicalKnowledgeCandidate,
        incoming: CanonicalKnowledgeCandidate
    ): CanonicalKnowledgeCandidate {

        val dimensionsByType =
            linkedMapOf<KnowledgeDimensionCandidateType, KnowledgeDimensionCandidate>()

        existing.dimensions.forEach { dimension ->
            dimensionsByType[dimension.dimension] =
                dimension
        }

        incoming.dimensions.forEach { dimension ->
            dimensionsByType.putIfAbsent(
                dimension.dimension,
                dimension
            )
        }

        return existing.copy(
            aliases =
                (existing.aliases + incoming.aliases)
                    .take(3)
                    .toSet(),

            matchAliases =
                emptySet(),

            dimensions =
                dimensionsByType
                    .values
                    .toList(),

            metadata =
                existing.metadata.copy(
                    attributes =
                        emptyMap()
                )
        )
    }

    fun finish(): KnowledgeCandidateMergeResult =
        KnowledgeCandidateMergeResult(
            candidates =
                candidates()
                    .toList(),

            conflicts =
                emptyList()
        )

    fun candidateCount(): Int =
        candidatesByKey.size

    fun conflictCount(): Int =
        conflicts

    fun blockedHighFanoutKeys(): Map<String, Int> =
        emptyMap()

    private fun mergeKey(
        candidate: CanonicalKnowledgeCandidate
    ): String =
        candidate.canonicalId
            .trim()
            .lowercase()
}