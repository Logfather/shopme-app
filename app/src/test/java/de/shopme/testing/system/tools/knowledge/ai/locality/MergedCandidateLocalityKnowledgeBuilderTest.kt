package de.shopme.testing.system.tools.knowledge.ai.locality

import de.shopme.tools.knowledge.ai.builder.locality.MergedCandidateLocalityKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.locality.Locality
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateLocalityKnowledgeBuilderTest {

    @Test
    fun buildsLocalityKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateLocalityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            locality = "REGIONAL"
                        )
                    )
                )

        assertEquals(
            Locality.REGIONAL,
            knowledge.entries["apple"]
        )
    }

    @Test
    fun ignoresUnknownLocalityValues() {
        val knowledge =
            MergedCandidateLocalityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            locality = "not_a_real_locality"
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
            MergedCandidateLocalityKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", "NATIONWIDE"),
                        candidate("apple", "REGIONAL"),
                        candidate("banana", "OVERSEAS")
                    )
                )

        assertEquals(
            listOf(
                "apple",
                "banana",
                "milk"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        locality: String
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.LOCALITY,
                        payload =
                            mapOf(
                                "locality" to locality
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