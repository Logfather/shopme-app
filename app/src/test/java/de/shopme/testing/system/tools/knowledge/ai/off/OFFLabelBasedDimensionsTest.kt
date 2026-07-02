package de.shopme.testing.system.tools.knowledge.ai.off

import de.shopme.tools.knowledge.ai.builder.AIKnowledgeBuildRequest
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceInfo
import de.shopme.tools.knowledge.ai.builder.AIKnowledgeSourceType
import de.shopme.tools.knowledge.ai.builder.RawKnowledgeInput
import de.shopme.tools.knowledge.ai.builder.off.DeterministicOFFCandidateBuilder
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import org.junit.Test
import kotlin.test.assertTrue

class OFFLabelBasedDimensionsTest {

    @Test
    fun createsLabelBasedDimensionsFromOffFields() {

        val request =
            AIKnowledgeBuildRequest(
                source = AIKnowledgeSourceInfo(
                    type = AIKnowledgeSourceType.OPEN_FOOD_FACTS,
                    name = "Open Food Facts",
                    version = "test"
                ),
                inputs = listOf(
                    RawKnowledgeInput(
                        sourceId = "00000758",
                        fields = mapOf(
                            "name" to "Test Food",
                            "allergens" to "en:milk,en:nuts",
                            "packaging" to "plastic tray",
                            "production" to "Germany",
                            "locality" to mapOf(
                                "countries" to "Germany",
                                "origins" to "France"
                            ),
                            "fairtrade" to mapOf(
                                "labels" to "Fairtrade"
                            ),
                            "animalWelfare" to mapOf(
                                "labels" to "organic, free range"
                            ),
                            "processing" to 4
                        )
                    )
                )
            )

        val result =
            DeterministicOFFCandidateBuilder()
                .build(request)

        val dimensions =
            result.candidates
                .single()
                .dimensions
                .map { it.dimension }
                .toSet()

        assertTrue(dimensions.contains(KnowledgeDimensionCandidateType.ALLERGENS))
        assertTrue(dimensions.contains(KnowledgeDimensionCandidateType.PACKAGING))
        assertTrue(dimensions.contains(KnowledgeDimensionCandidateType.PRODUCTION))
        assertTrue(dimensions.contains(KnowledgeDimensionCandidateType.LOCALITY))
        assertTrue(dimensions.contains(KnowledgeDimensionCandidateType.FAIRTRADE))
        assertTrue(dimensions.contains(KnowledgeDimensionCandidateType.ANIMAL_WELFARE))
        assertTrue(dimensions.contains(KnowledgeDimensionCandidateType.PROCESSING))
    }
}