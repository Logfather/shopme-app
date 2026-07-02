package de.shopme.tools.knowledge.ai.openai

import de.shopme.tools.knowledge.ai.AIProvider
import de.shopme.tools.knowledge.ai.AIProviderResponseParser
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeCandidateExtractor
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate

class OpenAIKnowledgeCandidateExtractor(
    private val provider: AIProvider,
    private val promptBuilder: GenericAIKnowledgePromptBuilder,
    private val parser: AIProviderResponseParser
) : AIKnowledgeCandidateExtractor {

    override fun extract(
        request: AIKnowledgeBuildRequest
    ): List<CanonicalKnowledgeCandidate> {

        val prompt = promptBuilder.build(request)

        val response = provider.complete(prompt)

        val batch = parser.parse(response)

        return batch.candidates
    }
}