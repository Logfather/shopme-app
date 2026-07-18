package de.shopme.testing.system.tools.knowledge.ki_candidates

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeCandidateMerger
import de.shopme.tools.knowledge.ki_candidates.KnowledgeConflictResolutionType
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class KnowledgeCandidateMergerConflictTest {

    @Test
    fun reportsConflictWhenSameDimensionHasDifferentPayloads() {

        val offCandidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "banana",
                aliases = setOf("Banana"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.CARBON,
                        payload = 0.82
                    )
                ),
                metadata = CandidateMetadata(
                    source = "off",
                    sourceId = "1",
                    confidence = 1.0,
                    version = "1"
                )
            )

        val agribalyseCandidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "banana",
                aliases = setOf("Banane"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.CARBON,
                        payload = 0.95
                    )
                ),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = "2",
                    confidence = 1.0,
                    version = "3.2"
                )
            )

        val result = KnowledgeCandidateMerger().merge(
            listOf(
                offCandidate,
                agribalyseCandidate
            )
        )

        assertEquals(1, result.candidates.size)
        assertEquals(1, result.conflicts.size)

        val conflict = result.conflicts.first()

        assertEquals("banana", conflict.canonicalId)
        assertEquals(
            KnowledgeDimensionCandidateType.CARBON,
            conflict.dimension
        )

        assertEquals(0.95, conflict.selectedPayload)

        assertTrue(
            conflict.rejectedPayloads.contains(0.82)
        )

        assertNull(conflict.resolution)

        val merged = result.candidates.single()

        val carbon = merged.dimensions.single {
            it.dimension == KnowledgeDimensionCandidateType.CARBON
        }

        assertEquals(0.95, carbon.payload)
    }

    @Test
    fun prefersOFFWhenNutritionPayloadConflicts() {

        val offCandidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "banana",
                aliases = setOf("Banana"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.NUTRITION,
                        payload = mapOf(
                            "caloriesPer100g" to 89
                        )
                    )
                ),
                metadata = CandidateMetadata(
                    source = "off",
                    sourceId = "off-banana",
                    confidence = 1.0,
                    version = "1"
                )
            )

        val agribalyseCandidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "banana",
                aliases = setOf("Banane"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.NUTRITION,
                        payload = mapOf(
                            "caloriesPer100g" to 92
                        )
                    )
                ),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = "agribalyse-banana",
                    confidence = 1.0,
                    version = "3.2"
                )
            )

        val result = KnowledgeCandidateMerger().merge(
            listOf(
                agribalyseCandidate,
                offCandidate
            )
        )

        assertEquals(1, result.candidates.size)
        assertEquals(1, result.conflicts.size)

        val merged = result.candidates.single()

        val nutrition = merged.dimensions.single {
            it.dimension == KnowledgeDimensionCandidateType.NUTRITION
        }

        assertEquals(
            mapOf("caloriesPer100g" to 89),
            nutrition.payload
        )
    }

    @Test
    fun whenNutritionConflictIsResolvedByQualityScoreThenConflictContainsResolutionMetadata() {
        val weakNutrition = KnowledgeDimensionCandidate(
            dimension = KnowledgeDimensionCandidateType.NUTRITION,
            payload = mapOf(
                "energyKcalPer100g" to 20.0
            )
        )

        val strongNutrition = KnowledgeDimensionCandidate(
            dimension = KnowledgeDimensionCandidateType.NUTRITION,
            payload = mapOf(
                "energyKcalPer100g" to 20.0,
                "fatPer100g" to 0.1,
                "saturatedFatPer100g" to 0.0,
                "carbohydratesPer100g" to 4.0,
                "sugarsPer100g" to 1.0,
                "fiberPer100g" to 0.5,
                "proteinsPer100g" to 0.8,
                "saltPer100g" to 0.01
            )
        )

        val first = CanonicalKnowledgeCandidate(
            canonicalId = "green tea",
            aliases = setOf("green tea"),
            dimensions = listOf(weakNutrition),
            metadata = CandidateMetadata(
                source = "open_food_facts",
                sourceId = "111",
                confidence = 0.8,
                version = "test"
            )
        )

        val second = CanonicalKnowledgeCandidate(
            canonicalId = "green tea",
            aliases = setOf("green tea"),
            dimensions = listOf(strongNutrition),
            metadata = CandidateMetadata(
                source = "open_food_facts",
                sourceId = "222",
                confidence = 0.8,
                version = "test"
            )
        )

        val result = KnowledgeCandidateMerger().merge(listOf(first, second))

        val conflict = result.conflicts.single()

        assertEquals(
            KnowledgeDimensionCandidateType.NUTRITION,
            conflict.dimension
        )

        assertNotNull(conflict.resolution)

        assertEquals(
            KnowledgeConflictResolutionType.QUALITY_SCORE,
            conflict.resolution?.type
        )
        assertEquals(2, conflict.resolution?.alternatives)
        assertNotNull(conflict.resolution?.selectedScore)
        assertTrue(conflict.resolution?.rejectedScores?.isNotEmpty() == true)
        assertNotNull(conflict.resolution?.confidence)
    }
}