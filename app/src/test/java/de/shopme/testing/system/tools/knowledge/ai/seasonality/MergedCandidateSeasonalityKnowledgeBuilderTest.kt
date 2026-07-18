package de.shopme.testing.system.tools.knowledge.ai.seasonality

import de.shopme.tools.knowledge.ai.builder.seasonality.MergedCandidateSeasonalityKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateSeasonalityKnowledgeBuilderTest {

    @Test
    fun buildsSeasonalityKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateSeasonalityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "strawberry",
                            months =
                                listOf(
                                    5,
                                    6,
                                    7
                                )
                        )
                    )
                )

        assertEquals(
            listOf(
                5,
                6,
                7
            ),
            knowledge.entries["strawberry"]
        )
    }

    @Test
    fun ignoresInvalidSeasonalityValues() {
        val knowledge =
            MergedCandidateSeasonalityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            months =
                                listOf(
                                    0,
                                    13
                                )
                        )
                    )
                )

        assertFalse(
            knowledge.entries.containsKey("invalid")
        )
    }

    @Test
    fun normalizesMonths() {
        val knowledge =
            MergedCandidateSeasonalityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            months =
                                listOf(
                                    10,
                                    9,
                                    10,
                                    8
                                )
                        )
                    )
                )

        assertEquals(
            listOf(
                8,
                9,
                10
            ),
            knowledge.entries["apple"]
        )
    }

    @Test
    fun sortsEntriesByCanonicalId() {
        val knowledge =
            MergedCandidateSeasonalityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("tomato", listOf(7, 8, 9)),
                        candidate("apple", listOf(8, 9, 10)),
                        candidate("strawberry", listOf(5, 6, 7))
                    )
                )

        assertEquals(
            listOf(
                "apple",
                "strawberry",
                "tomato"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        months: List<Int>
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.SEASONALITY,
                        payload =
                            mapOf(
                                "months" to months
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