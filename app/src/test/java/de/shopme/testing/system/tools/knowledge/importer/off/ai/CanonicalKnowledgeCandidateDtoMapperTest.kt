package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.dto.CandidateMetadataDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateAIResponseDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDtoMapper
import de.shopme.tools.knowledge.ai.dto.KnowledgeDimensionCandidateDto
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CanonicalKnowledgeCandidateDtoMapperTest {

    @Test
    fun mapCreatesCanonicalKnowledgeCandidatesFromDto() {

        val mapper = CanonicalKnowledgeCandidateDtoMapper()

        val result = mapper.map(
            CanonicalKnowledgeCandidateAIResponseDto(
                schemaVersion = "canonical_knowledge_candidate_response_v1",
                candidates = listOf(
                    CanonicalKnowledgeCandidateDto(
                        canonicalId = "apple",
                        aliases = listOf("apple", "apfel"),
                        dimensions = listOf(
                            KnowledgeDimensionCandidateDto(
                                type = "TAXONOMY",
                                payload = "fruit"
                            )
                        ),
                        metadata = CandidateMetadataDto(
                            source = "open_food_facts",
                            confidence = 1.0
                        )
                    )
                )
            )
        )

        assertEquals(1, result.size)

        val candidate = result.first()

        assertEquals("apple", candidate.canonicalId)
        assertTrue(candidate.aliases.contains("apple"))
        assertTrue(candidate.aliases.contains("apfel"))

        assertEquals(1, candidate.dimensions.size)

        val dimension = candidate.dimensions.first()

        assertEquals(
            KnowledgeDimensionCandidateType.TAXONOMY,
            dimension.dimension
        )

        assertEquals(
            "fruit",
            dimension.payload
        )

        assertEquals(
            "open_food_facts",
            candidate.metadata.source
        )

        assertEquals(
            1.0,
            candidate.metadata.confidence,
            0.0
        )

        assertEquals(
            "canonical_knowledge_candidate_response_v1",
            candidate.metadata.version
        )
    }
}