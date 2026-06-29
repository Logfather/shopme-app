package de.shopme.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationResult

data class FoodsPatchCompileResult(

    val candidates: List<CanonicalKnowledgeCandidate>,

    val validationResult: FoodsPatchValidationResult
)