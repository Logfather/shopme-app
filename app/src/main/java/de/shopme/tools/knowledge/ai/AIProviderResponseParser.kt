package de.shopme.tools.knowledge.ai

import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch

interface AIProviderResponseParser {

    fun parse(
        response: AIProviderResponse
    ): KnowledgeImportBatch
}