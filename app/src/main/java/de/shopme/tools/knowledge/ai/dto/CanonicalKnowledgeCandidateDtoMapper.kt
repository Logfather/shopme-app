package de.shopme.tools.knowledge.ai.dto

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType

class CanonicalKnowledgeCandidateDtoMapper {

    fun map(
        response: CanonicalKnowledgeCandidateAIResponseDto
    ): List<CanonicalKnowledgeCandidate> {

        return response.candidates.map { candidate ->

            require(
                !candidate.canonicalId.isNullOrBlank()
            ) {
                "Missing candidate canonicalId."
            }

            val metadata = candidate.metadata
                ?: throw IllegalArgumentException("Missing candidate metadata.")

            require(
                !metadata.source.isNullOrBlank()
            ) {
                "Missing candidate metadata source."
            }

            require(
                metadata.confidence != null
            ) {
                "Missing candidate metadata confidence."
            }

            val dimensions = candidate.dimensions
                ?: throw IllegalArgumentException("Missing candidate dimensions.")

            require(
                dimensions.isNotEmpty()
            ) {
                "Missing candidate dimensions."
            }

            CanonicalKnowledgeCandidate(
                canonicalId = candidate.canonicalId,
                aliases = candidate.aliases.orEmpty().toSet(),
                dimensions = dimensions.map { dimension ->
                    mapDimension(dimension)
                },
                metadata = CandidateMetadata(
                    source = metadata.source,
                    sourceId = null,
                    confidence = metadata.confidence,
                    version = response.schemaVersion
                )
            )
        }
    }

    private fun mapDimension(
        dimension: KnowledgeDimensionCandidateDto
    ): KnowledgeDimensionCandidate {

        require(
            !dimension.type.isNullOrBlank()
        ) {
            "Missing candidate dimension type."
        }

        val dimensionType = try {

            KnowledgeDimensionCandidateType.valueOf(
                dimension.type
            )

        } catch (exception: IllegalArgumentException) {

            throw IllegalArgumentException(
                "Unsupported knowledge dimension: ${dimension.type}",
                exception
            )
        }

        return KnowledgeDimensionCandidate(
            dimension = dimensionType,
            payload = dimension.payload
        )
    }
}