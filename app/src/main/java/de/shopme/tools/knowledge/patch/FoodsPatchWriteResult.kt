package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

data class FoodsPatchWriteResult(

    val candidates: List<CanonicalKnowledgeCandidate>,

    val applyResult: FoodsPatchApplyResult,

    val serializedJson: String,

    val stats: FoodsPatchWriteStats

)