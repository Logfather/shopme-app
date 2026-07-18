package de.shopme.testing.system.tools.knowledge.ai.pesticides

import de.shopme.tools.knowledge.ai.builder.pesticides.MergedCandidatePesticidesKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidatePesticidesKnowledgeBuilderTest {

    @Test
    fun buildsPesticidesKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidatePesticidesKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            score = 0.75
                        )
                    )
                )

        assertEquals(
            0.75,
            knowledge.entries["apple"]?.score
        )
    }

    @Test
    fun ignoresInvalidPesticideValues() {
        val knowledge =
            MergedCandidatePesticidesKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            score = 1.5
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
            MergedCandidatePesticidesKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", 0.1),
                        candidate("apple", 0.75),
                        candidate("bread", 0.4)
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
        score: Double
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.PESTICIDES,
                        payload =
                            mapOf(
                                "score" to score
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