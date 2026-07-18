package de.shopme.testing.system.tools.knowledge.ai.waterstress

import de.shopme.tools.knowledge.ai.builder.waterstress.MergedCandidateWaterStressKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateWaterStressKnowledgeBuilderTest {

    @Test
    fun buildsWaterStressKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateWaterStressKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "almond",
                            score = 12.5
                        )
                    )
                )

        assertEquals(
            12.5,
            knowledge.entries["almond"]?.score
        )
    }

    @Test
    fun ignoresInvalidWaterStressValues() {
        val knowledge =
            MergedCandidateWaterStressKnowledgeBuilder()
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
            MergedCandidateWaterStressKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", 3.0),
                        candidate("almond", 12.5),
                        candidate("bread", 1.2)
                    )
                )

        assertEquals(
            listOf(
                "almond",
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
                            KnowledgeDimensionCandidateType.WATER_STRESS,
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