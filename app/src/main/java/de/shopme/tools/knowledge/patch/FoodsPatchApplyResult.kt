package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class FoodsPatchApplyResult(

    val candidates: List<CanonicalKnowledgeCandidate>,

    val compileResult: FoodsPatchCompileResult,

    val diff: FoodsPatchDiff,

    val stats: FoodsPatchApplyStats

)