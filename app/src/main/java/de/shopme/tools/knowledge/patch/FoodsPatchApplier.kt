package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class FoodsPatchApplier(
    private val diffCalculator: FoodsPatchDiffCalculator,
    private val compiler: FoodsPatchCompiler
) {

    fun apply(
        existingCandidates: List<CanonicalKnowledgeCandidate>,
        patch: FoodsKnowledgePatch
    ): FoodsPatchApplyResult {

        val compileResult =
            compiler.compile(
                existingCandidates = existingCandidates,
                patch = patch
            )

        val stats =
            FoodsPatchApplyStats(
                candidateCountBefore = existingCandidates.size,
                candidateCountAfter = compileResult.candidates.size,
                patchEntryCount = patch.entries.size
            )

        val diff =
            diffCalculator.calculate(
                existingCandidates = existingCandidates,
                patch = patch
            )

        return FoodsPatchApplyResult(
            candidates = compileResult.candidates,
            compileResult = compileResult,
            diff = diff,
            stats = stats
        )
    }
}