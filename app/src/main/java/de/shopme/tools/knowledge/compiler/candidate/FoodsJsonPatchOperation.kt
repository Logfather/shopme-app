package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch

data class FoodsJsonPatchOperation(
    val canonicalId: String,
    val type: FoodsJsonPatchOperationType,
    val candidate: CandidateFoodKnowledgePatch
)