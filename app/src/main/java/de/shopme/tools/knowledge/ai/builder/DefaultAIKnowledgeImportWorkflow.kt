package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchFactory
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata

class DefaultAIKnowledgeImportWorkflow(
    private val builder: AIKnowledgeBuilder,
    private val batchFactory: KnowledgeImportBatchFactory
) : AIKnowledgeImportWorkflow {

    override fun import(
        request: AIKnowledgeBuildRequest,
        metadata: KnowledgeImportBatchMetadata
    ): KnowledgeImportBatch {

        val result = builder.build(request)

        return batchFactory.create(
            result = result,
            metadata = metadata
        )
    }
}