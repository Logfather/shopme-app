package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata

class DefaultAIKnowledgeCandidateProcessor : AIKnowledgeCandidateProcessor {

    override fun process(
        result: AIKnowledgeBuildResult
    ): KnowledgeImportBatch {

        return KnowledgeImportBatch(
            candidates = result.candidates,
            metadata = KnowledgeImportBatchMetadata(
                source = "ai",
                generatedBy = "AIKnowledgeBuilder",
                generatedAt = "unknown"
            )
        )
    }
}