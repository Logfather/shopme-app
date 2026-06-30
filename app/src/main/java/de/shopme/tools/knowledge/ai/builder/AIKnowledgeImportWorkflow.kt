package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata

interface AIKnowledgeImportWorkflow {

    fun import(
        request: AIKnowledgeBuildRequest,
        metadata: KnowledgeImportBatchMetadata
    ): KnowledgeImportBatch
}
