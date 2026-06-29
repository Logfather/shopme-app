package de.shopme.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.AIExtractionClient
import de.shopme.tools.knowledge.ai.AIProvider
import de.shopme.tools.knowledge.ai.AIProviderResponseParser
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch

class ExternalOFFAIExtractionClient(
    private val provider: AIProvider,
    private val promptBuilder: OFFPromptBuilder,
    private val parser: AIProviderResponseParser
) : AIExtractionClient<OFFAIExtractionBatch> {

    override fun extract(
        input: OFFAIExtractionBatch
    ): KnowledgeImportBatch {

        val request = promptBuilder.build(input)

        val response = provider.complete(request)

        println("RAW AI RESPONSE:")
        println(response.content)

        return parser.parse(response)
    }
}