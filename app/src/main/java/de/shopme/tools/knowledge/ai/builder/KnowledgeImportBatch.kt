package de.shopme.tools.knowledge.ai.builder

import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch

interface AIKnowledgeCandidateProcessor {

    fun process(
        result: AIKnowledgeBuildResult
    ): KnowledgeImportBatch
}