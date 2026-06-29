package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class FoodsPatchMergeEngine {

    fun merge(
        existingCandidates: List<CanonicalKnowledgeCandidate>,
        patch: FoodsKnowledgePatch
    ): List<CanonicalKnowledgeCandidate> {

        val mergedById: MutableMap<String, CanonicalKnowledgeCandidate> =
            existingCandidates
                .associateBy { candidate ->
                    candidate.canonicalId
                }
                .toMutableMap()

        patch.entries.forEach { patchEntry ->

            mergedById[
                patchEntry.candidate.canonicalId
            ] = patchEntry.candidate
        }

        return mergedById
            .values
            .sortedBy { candidate ->
                candidate.canonicalId
            }
    }
}