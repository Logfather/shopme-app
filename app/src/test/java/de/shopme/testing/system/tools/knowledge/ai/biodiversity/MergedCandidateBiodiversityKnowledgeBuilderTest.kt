package de.shopme.testing.system.tools.knowledge.ai.biodiversity

import de.shopme.tools.knowledge.ai.builder.biodiversity.MergedCandidateBiodiversityKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateBiodiversityKnowledgeBuilderTest {

    @Test
    fun buildsBiodiversityKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateBiodiversityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "almond",
                            score = 72.5
                        )
                    )
                )

        assertEquals(
            72.5,
            knowledge.entries["almond"]?.score
        )
    }

    @Test
    fun ignoresInvalidBiodiversityValues() {
        val knowledge =
            MergedCandidateBiodiversityKnowledgeBuilder()
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
            MergedCandidateBiodiversityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", 30.0),
                        candidate("almond", 72.5),
                        candidate("bread", 45.0)
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
                            KnowledgeDimensionCandidateType.BIODIVERSITY,
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