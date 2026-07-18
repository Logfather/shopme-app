package de.shopme.testing.system.tools.knowledge.ai.nutriscore

import de.shopme.tools.knowledge.ai.builder.nutriscore.MergedCandidateNutriScoreKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.nutriscore.NutriScore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateNutriScoreKnowledgeBuilderTest {

    @Test
    fun buildsNutriScoreKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateNutriScoreKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            score = "A"
                        )
                    )
                )

        assertEquals(
            NutriScore.A,
            knowledge.entries["apple"]
        )
    }

    @Test
    fun ignoresUnknownNutriScoreValues() {
        val knowledge =
            MergedCandidateNutriScoreKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            score = "Z"
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
            MergedCandidateNutriScoreKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", "B"),
                        candidate("apple", "A"),
                        candidate("chips", "D")
                    )
                )

        assertEquals(
            listOf(
                "apple",
                "chips",
                "milk"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        score: String
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.NUTRI_SCORE,
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