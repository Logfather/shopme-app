package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.tools.knowledge.ki_candidates.CandidateFoodKnowledgePatch

class DefaultKnowledgeImportBatchPatchMapper : KnowledgeImportBatchPatchMapper {

    override fun map(
        batch: KnowledgeImportBatch
    ): FoodsJsonPatch {

        return FoodsJsonPatch(
            operations = batch.candidates.map { candidate ->

                FoodsJsonPatchOperation(
                    canonicalId = candidate.canonicalId,
                    type = FoodsJsonPatchOperationType.ADD,
                    candidate = CandidateFoodKnowledgePatch(
                        canonicalId = candidate.canonicalId,
                        aliases = candidate.aliases,
                        dimensions = candidate.dimensions,
                        metadata = candidate.metadata
                    )
                )
            }
        )
    }
}