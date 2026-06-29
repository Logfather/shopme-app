package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class FoodsPatchDiffCalculator {

    fun calculate(
        existingCandidates: List<CanonicalKnowledgeCandidate>,
        patch: FoodsKnowledgePatch
    ): FoodsPatchDiff {

        val existingIds =
            existingCandidates
                .map { candidate ->
                    candidate.canonicalId
                }
                .toSet()

        val entries =
            patch.entries
                .map { patchEntry ->

                    val operation =
                        if (patchEntry.canonicalId in existingIds) {
                            FoodsPatchDiffOperation.UPDATE
                        } else {
                            FoodsPatchDiffOperation.ADD
                        }

                    FoodsPatchDiffEntry(
                        canonicalId = patchEntry.canonicalId,
                        operation = operation
                    )
                }
                .sortedBy { entry ->
                    entry.canonicalId
                }

        val stats =
            FoodsPatchDiffStats(

                addedCount =
                    entries.count {
                        it.operation == FoodsPatchDiffOperation.ADD
                    },

                updatedCount =
                    entries.count {
                        it.operation == FoodsPatchDiffOperation.UPDATE
                    },

                unchangedCount =
                    entries.count {
                        it.operation == FoodsPatchDiffOperation.UNCHANGED
                    }
            )

        return FoodsPatchDiff(
            entries = entries,
            stats = stats
        )
    }
}