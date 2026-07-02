package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.domain.catalog.CatalogItem
import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class CanonicalKnowledgeCandidateToPatchOperationMapper {

    fun map(
        catalog: List<CatalogItem>,
        candidate: CanonicalKnowledgeCandidate
    ): FoodsJsonPatchOperation {

        val operationType =
            if (catalog.any { it.normalized == candidate.canonicalId }) {
                FoodsJsonPatchOperationType.UPDATE
            } else {
                FoodsJsonPatchOperationType.ADD
            }

        return FoodsJsonPatchOperation(
            canonicalId = candidate.canonicalId,
            type = operationType,
            candidate = CandidateFoodKnowledgePatch(
                canonicalId = candidate.canonicalId,
                aliases = candidate.aliases,
                dimensions = candidate.dimensions,
                metadata = candidate.metadata
            )
        )
    }
}