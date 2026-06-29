package de.shopme.tools.knowledge.ai

import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch

interface AIExtractionClient<T> {

    fun extract(
        input: T
    ): KnowledgeImportBatch
}