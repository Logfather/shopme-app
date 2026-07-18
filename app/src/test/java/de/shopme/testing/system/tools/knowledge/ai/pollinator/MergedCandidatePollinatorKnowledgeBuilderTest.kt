package de.shopme.testing.system.tools.knowledge.ai.pollinator

import de.shopme.tools.knowledge.ai.builder.pollinator.MergedCandidatePollinatorKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidatePollinatorKnowledgeBuilderTest {

    @Test
    fun buildsPollinatorKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidatePollinatorKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            score = 85.0
                        )
                    )
                )

        assertEquals(
            85.0,
            knowledge.entries["apple"]?.score
        )
    }

    @Test
    fun ignoresInvalidPollinatorValues() {
        val knowledge =
            MergedCandidatePollinatorKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            score = -1.0
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
            MergedCandidatePollinatorKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", 10.0),
                        candidate("apple", 85.0),
                        candidate("almond", 95.0)
                    )
                )

        assertEquals(
            listOf(
                "almond",
                "apple",
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
                            KnowledgeDimensionCandidateType.POLLINATOR,
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