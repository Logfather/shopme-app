package de.shopme.testing.system.tools.knowledge.ai.water

import de.shopme.tools.knowledge.ai.builder.water.MergedCandidateWaterKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateWaterKnowledgeBuilderTest {

    @Test
    fun buildsWaterKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateWaterKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            litersPerKilogram = 822.0
                        )
                    )
                )

        assertEquals(
            822.0,
            knowledge
                .entries["apple"]
                ?.litersPerKilogram
        )
    }

    @Test
    fun ignoresInvalidWaterValues() {
        val knowledge =
            MergedCandidateWaterKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            litersPerKilogram = -1.0
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
            MergedCandidateWaterKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", 1000.0),
                        candidate("apple", 822.0),
                        candidate("bread", 1600.0)
                    )
                )

        assertEquals(
            listOf(
                "apple",
                "bread",
                "milk"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        litersPerKilogram: Double
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.WATER,
                        payload =
                            mapOf(
                                "litersPerKilogram" to
                                        litersPerKilogram
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