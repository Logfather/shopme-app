package de.shopme.testing.system.tools.knowledge.ai.production

import de.shopme.tools.knowledge.ai.builder.production.MergedCandidateProductionKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.production.ProductionMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateProductionKnowledgeBuilderTest {

    @Test
    fun buildsProductionKnowledgeFromCandidate() {
        val method =
            ProductionMethod.entries.first()

        val knowledge =
            MergedCandidateProductionKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            production =
                                listOf(
                                    method.name.lowercase()
                                )
                        )
                    )
                )

        assertEquals(
            setOf(method),
            knowledge.entries["apple"]
        )
    }

    @Test
    fun ignoresUnknownProductionMethods() {
        val knowledge =
            MergedCandidateProductionKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "invalid",
                            production =
                                listOf(
                                    "not_a_real_method"
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
        val method =
            ProductionMethod.entries.first()

        val knowledge =
            MergedCandidateProductionKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("milk", listOf(method.name)),
                        candidate("apple", listOf(method.name)),
                        candidate("bread", listOf(method.name))
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
        production: List<String>
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions =
                listOf(
                    KnowledgeDimensionCandidate(
                        dimension =
                            KnowledgeDimensionCandidateType.PRODUCTION,
                        payload =
                            mapOf(
                                "production" to production
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