package de.shopme.testing.system.tools.knowledge.ai.diet

import de.shopme.tools.knowledge.ai.builder.diet.MergedCandidateDietKnowledgeBuilder
import de.shopme.tools.knowledge.diet.DietClassification
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateDietKnowledgeBuilderTest {

    @Test
    fun buildsDietKnowledgeFromCandidate() {
        val classification =
            DietClassification.entries.first()

        val knowledge =
            MergedCandidateDietKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            classifications =
                                listOf(
                                    classification.name.lowercase()
                                )
                        )
                    )
                )

        assertEquals(
            setOf(classification),
            knowledge.entries["apple"]
        )
    }

    @Test
    fun ignoresUnknownDietClassifications() {
        val knowledge =
            MergedCandidateDietKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            classifications =
                                listOf(
                                    "not_a_real_diet"
                                )
                        )
                    )
                )

        assertFalse(
            knowledge.entries.containsKey("invalid")
        )
    }

    @Test
    fun sortsEntriesByCanonicalId() {
        val classification =
            DietClassification.entries.first()

        val knowledge =
            MergedCandidateDietKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", listOf(classification.name)),
                        candidate("apple", listOf(classification.name)),
                        candidate("bread", listOf(classification.name))
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
        classifications: List<String>
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.DIET,
                        payload =
                            mapOf(
                                "classifications" to classifications
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