package de.shopme.testing.system.tools.knowledge.ai.foodmiles

import de.shopme.tools.knowledge.ai.builder.foodmiles.MergedCandidateFoodMilesKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateFoodMilesKnowledgeBuilderTest {

    @Test
    fun buildsFoodMilesKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateFoodMilesKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "banana",
                            kilometers = 750.0
                        )
                    )
                )

        assertEquals(
            750.0,
            knowledge.entries["banana"]?.kilometers
        )
    }

    @Test
    fun ignoresInvalidFoodMilesValues() {
        val knowledge =
            MergedCandidateFoodMilesKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            kilometers = -1.0
                        )
                    )
                )

        assertFalse(
            knowledge.entries.containsKey("invalid")
        )
    }

    @Test
    fun sortsEntriesByCanonicalId() {
        val knowledge =
            MergedCandidateFoodMilesKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", 120.0),
                        candidate("banana", 750.0),
                        candidate("apple", 40.0)
                    )
                )

        assertEquals(
            listOf(
                "apple",
                "banana",
                "milk"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        kilometers: Double
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.FOOD_MILES,
                        payload =
                            mapOf(
                                "kilometers" to kilometers
                            )
                    )
                ),
            metadata =
                CandidateMetadata(
                    source = "test",
                    sourceId = canonicalId,
                    confidence = 1.0,
                    version = "test"
                )
        )
}