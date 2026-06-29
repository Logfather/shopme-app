package de.shopme.testing.system.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.dto.CandidateMetadataDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateAIResponseDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDto
import de.shopme.tools.knowledge.ai.dto.CanonicalKnowledgeCandidateDtoMapper
import de.shopme.tools.knowledge.ai.dto.KnowledgeDimensionCandidateDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CanonicalKnowledgeCandidateDtoMapperBlankDimensionTypeTest {

    @Test
    fun mapRejectsBlankDimensionType() {

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
                                    type = "   ",
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

            fail("Expected IllegalArgumentException.")

        } catch (exception: IllegalArgumentException) {

            assertEquals(
                "Missing candidate dimension type.",
                exception.message
            )
        }
    }
}