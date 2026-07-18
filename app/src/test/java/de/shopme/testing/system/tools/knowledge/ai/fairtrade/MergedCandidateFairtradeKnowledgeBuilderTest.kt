package de.shopme.testing.system.tools.knowledge.ai.fairtrade

import de.shopme.tools.knowledge.ai.builder.fairtrade.MergedCandidateFairtradeKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateFairtradeKnowledgeBuilderTest {

    @Test
    fun buildsFairtradeKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateFairtradeKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "banana",
                            score = 1.0
                        )
                    )
                )

        assertEquals(
            1.0,
            knowledge.entries["banana"]?.score
        )
    }

    @Test
    fun buildsNonFairtradeKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateFairtradeKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            score = 0.0
                        )
                    )
                )

        assertEquals(
            0.0,
            knowledge.entries["apple"]?.score
        )
    }

    @Test
    fun ignoresInvalidFairtradeValues() {
        val knowledge =
            MergedCandidateFairtradeKnowledgeBuilder()
                .build(
                    listOf(
                        invalidCandidate(
                            canonicalId = "invalid",
                            value = "unknown"
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
            MergedCandidateFairtradeKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("coffee", 1.0),
                        candidate("banana", 1.0),
                        candidate("cocoa", 1.0)
                    )
                )

        assertEquals(
            listOf(
                "banana",
                "cocoa",
                "coffee"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        score: Double
    ): CanonicalKnowledgeCandidate =
        rawCandidate(
            canonicalId = canonicalId,
            value = score
        )

    private fun invalidCandidate(
        canonicalId: String,
        value: Any
    ): CanonicalKnowledgeCandidate =
        rawCandidate(
            canonicalId = canonicalId,
            value = value
        )

    private fun rawCandidate(
        canonicalId: String,
        value: Any
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.FAIRTRADE,
                        payload =
                            mapOf(
                                "score" to value
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