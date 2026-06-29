package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.dto.CandidateMetadataDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateAIResponseDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDtoMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class CanonicalKnowledgeCandidateDtoMapperEmptyDimensionsTest {

    @Test
    fun mapRejectsEmptyDimensions() {

        val mapper = CanonicalKnowledgeCandidateDtoMapper()

        try {

            mapper.map(
                CanonicalKnowledgeCandidateAIResponseDto(
                    schemaVersion = "canonical_knowledge_candidate_response_v1",
                    candidates = listOf(
                        CanonicalKnowledgeCandidateDto(
                            canonicalId = "apple",
                            aliases = listOf("apple"),
                            dimensions = emptyList(),
                            metadata = CandidateMetadataDto(
                                source = "open_food_facts",
                                confidence = 1.0
                            )
                        )
                    )
                )
            )

            fail("Expected IllegalArgumentException.")

        } catch (exception: IllegalArgumentException) {

            assertEquals(
                "Missing candidate dimensions.",
                exception.message
            )
        }
    }
}