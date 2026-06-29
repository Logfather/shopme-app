package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidator

class FoodsPatchCompiler(

    private val validator: FoodsPatchValidator,
    private val mergeEngine: FoodsPatchMergeEngine

) {

    fun compile(
        existingCandidates: List<CanonicalKnowledgeCandidate>,
        patch: FoodsKnowledgePatch
    ): FoodsPatchCompileResult {

        val validationResult =
            validator.validate(patch)

        if (!validationResult.isValid) {
            throw IllegalStateException(
                "Foods patch validation failed."
            )
        }

        val mergedCandidates =
            mergeEngine.merge(
                existingCandidates = existingCandidates,
                patch = patch
            )

        return FoodsPatchCompileResult(
            candidates = mergedCandidates,
            validationResult = validationResult
        )
    }
}