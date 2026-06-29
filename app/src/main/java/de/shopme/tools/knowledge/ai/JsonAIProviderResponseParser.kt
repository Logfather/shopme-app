package de.shopme.tools.knowledge.ai

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateAIResponseDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDtoMapper
import de.shopme.tools.knowledge.ai.schema.CanonicalKnowledgeCandidateAIResponseSchema
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatch
import de.shopme.tools.knowledge.compiler.candidate.KnowledgeImportBatchMetadata
import java.time.Instant

class JsonAIProviderResponseParser(
    private val gson: Gson = Gson(),
    private val dtoMapper: CanonicalKnowledgeCandidateDtoMapper =
        CanonicalKnowledgeCandidateDtoMapper()
) : AIProviderResponseParser {

    override fun parse(
        response: AIProviderResponse
    ): KnowledgeImportBatch {

        val dto = try {

            gson.fromJson(
                response.content,
                CanonicalKnowledgeCandidateAIResponseDto::class.java
            )

        } catch (exception: JsonSyntaxException) {

            throw IllegalArgumentException(
                "Invalid AI response JSON.",
                exception
            )
        }

        val schemaVersion = dto.schemaVersion

        require(!schemaVersion.isNullOrBlank()) {
            "Missing AI response schema version."
        }

        require(
            schemaVersion == CanonicalKnowledgeCandidateAIResponseSchema.VERSION
        ) {
            "Unsupported AI response schema: $schemaVersion"
        }

        val candidates = dtoMapper.map(dto)

        return KnowledgeImportBatch(
            candidates = candidates,
            metadata = KnowledgeImportBatchMetadata(
                source = "ai",
                generatedBy = "json-ai-provider-response-parser",
                generatedAt = Instant.now().toString(),
                promptVersion = dto.schemaVersion
            )
        )
    }
}