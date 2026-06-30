package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult

class DefaultKnowledgeImportBatchFactory : KnowledgeImportBatchFactory {

    override fun create(
        result: AIKnowledgeBuildResult,
        metadata: KnowledgeImportBatchMetadata
    ): KnowledgeImportBatch {

        return KnowledgeImportBatch(
            candidates = result.candidates,
            metadata = metadata
        )
    }
}