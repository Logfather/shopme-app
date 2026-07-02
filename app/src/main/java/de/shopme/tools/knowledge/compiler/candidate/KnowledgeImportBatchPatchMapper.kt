package de.shopme.tools.knowledge.compiler.candidate

interface KnowledgeImportBatchPatchMapper {

    fun map(
        batch: KnowledgeImportBatch
    ): FoodsJsonPatch
}