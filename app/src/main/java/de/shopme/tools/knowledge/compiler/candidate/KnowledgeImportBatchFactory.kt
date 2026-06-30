package de.shopme.tools.knowledge.compiler.candidate

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildResult

interface KnowledgeImportBatchFactory {

    fun create(
        result: AIKnowledgeBuildResult,
        metadata: KnowledgeImportBatchMetadata
    ): KnowledgeImportBatch
}