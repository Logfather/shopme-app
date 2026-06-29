package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.dto.CandidateMetadataDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateAIResponseDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDtoMapper
import de.shopme.tools.knowledge.ai.dto.KnowledgeDimensionCandidateDto
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class CanonicalKnowledgeCandidateDtoMapperMissingMetadataConfidenceTest {

    @Test
    fun mapRejectsMissingMetadataConfidence() {

        val mapper = CanonicalKnowledgeCandidateDtoMapper()

        try {

            mapper.map(
                CanonicalKnowledgeCandidateAIResponseDto(
                    schemaVersion = "canonical_knowledge_candidate_response_v1",
                    candidates = listOf(
                        CanonicalKnowledgeCandidateDto(
                            canonicalId = "apple",
                            aliases = listOf("apple"),
                            dimensions = listOf(
                                KnowledgeDimensionCandidateDto(
                                    type = "TAXONOMY",
                                    payload = "fruit"
                                )
                            ),
                            metadata = CandidateMetadataDto(
                                source = "open_food_facts",
                                confidence = null
                            )
                        )
                    )
                )
            )

            fail("Expected IllegalArgumentException.")

        } catch (exception: IllegalArgumentException) {

            assertEquals(
                "Missing candidate metadata confidence.",
                exception.message
            )
        }
    }
}