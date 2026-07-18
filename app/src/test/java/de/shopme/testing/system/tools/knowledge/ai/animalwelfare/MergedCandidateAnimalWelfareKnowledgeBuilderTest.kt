package de.shopme.testing.system.tools.knowledge.ai.animalwelfare

import de.shopme.tools.knowledge.ai.builder.animalwelfare.MergedCandidateAnimalWelfareKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateAnimalWelfareKnowledgeBuilderTest {

    @Test
    fun buildsAnimalWelfareKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateAnimalWelfareKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "egg",
                            score = 0.8
                        )
                    )
                )

        assertEquals(
            0.8,
            knowledge.entries["egg"]?.score
        )
    }

    @Test
    fun ignoresInvalidAnimalWelfareValues() {
        val knowledge =
            MergedCandidateAnimalWelfareKnowledgeBuilder()
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
            MergedCandidateAnimalWelfareKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", 0.5),
                        candidate("egg", 0.8),
                        candidate("cheese", 0.6)
                    )
                )

        assertEquals(
            listOf(
                "cheese",
                "egg",
                "milk"
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
                            KnowledgeDimensionCandidateType.ANIMAL_WELFARE,
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