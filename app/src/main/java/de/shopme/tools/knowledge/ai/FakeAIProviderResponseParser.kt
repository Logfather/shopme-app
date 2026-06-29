package de.shopme.tools.knowledge.ai

import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata

class FakeAIProviderResponseParser : AIProviderResponseParser {

    override fun parse(
        response: AIProviderResponse
    ): KnowledgeImportBatch {

        return KnowledgeImportBatch(
            candidates = emptyList(),
            metadata = KnowledgeImportBatchMetadata(
                source = "open_food_facts",
                generatedBy = "fake_ai_provider_response_parser",
                generatedAt = "test",
                promptVersion = null
            )
        )
    }
}