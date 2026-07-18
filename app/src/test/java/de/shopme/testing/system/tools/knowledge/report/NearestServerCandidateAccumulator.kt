package de.shopme.testing.system.tools.knowledge.report

internal class NearestServerCandidateAccumulator(
    private val limit: Int
) {

    init {
        require(limit > 0) {
            "limit must be greater than zero"
        }
    }

    private val candidates =
        mutableListOf<NearestServerKnowledgeCandidate>()

    fun offer(candidate: NearestServerKnowledgeCandidate) {

        if (candidate.score <= 0.0) {
            return
        }

        val existingIndex =
            candidates.indexOfFirst {
                it.serverKey == candidate.serverKey
            }

        if (existingIndex >= 0) {
            if (candidate.score > candidates[existingIndex].score) {
                candidates[existingIndex] = candidate
            }
        } else {
            candidates += candidate
        }

        candidates.sortWith(
            compareByDescending<NearestServerKnowledgeCandidate> {
                it.score
            }.thenBy {
                it.serverKey
            }
        )

        if (candidates.size > limit) {
            candidates.subList(
                limit,
                candidates.size
            ).clear()
        }
    }

    fun result(): List<NearestServerKnowledgeCandidate> =
        candidates.toList()
}