package de.shopme.testing.system.tools.knowledge.ai.recipegraph

import de.shopme.tools.knowledge.ai.builder.recipegraph.MergedCandidateRecipeGraphKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateRecipeGraphKnowledgeBuilderTest {

    @Test
    fun buildsRecipeGraphKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateRecipeGraphKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "tomato",
                            recipes =
                                listOf(
                                    "tomato_sauce",
                                    "bruschetta"
                                )
                        )
                    )
                )

        assertEquals(
            setOf(
                "bruschetta",
                "tomato_sauce"
            ),
            knowledge.entries["tomato"]
                ?.ingredients
        )
    }

    @Test
    fun ignoresInvalidRecipeGraphValues() {
        val knowledge =
            MergedCandidateRecipeGraphKnowledgeBuilder()
                .build(
                    listOf(
                        invalidCandidate(
                            canonicalId = "invalid",
                            value = 123
                        )
                    )
                )

        assertFalse(
            knowledge.entries.containsKey("invalid")
        )
    }

    @Test
    fun normalizesRecipeGraphEntries() {
        val knowledge =
            MergedCandidateRecipeGraphKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "apple",
                            recipes =
                                listOf(
                                    " apple_pie ",
                                    "apple_crumble",
                                    "apple_pie",
                                    ""
                                )
                        )
                    )
                )

        assertEquals(
            setOf(
                "apple_crumble",
                "apple_pie"
            ),
            knowledge.entries["apple"]
                ?.ingredients
        )
    }

    @Test
    fun sortsEntriesByCanonicalId() {
        val knowledge =
            MergedCandidateRecipeGraphKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("tomato", listOf("tomato_sauce")),
                        candidate("apple", listOf("apple_pie")),
                        candidate("strawberry", listOf("strawberry_jam"))
                    )
                )

        assertEquals(
            listOf(
                "apple",
                "strawberry",
                "tomato"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        recipes: List<String>
    ): CanonicalKnowledgeCandidate =
        rawCandidate(
            canonicalId = canonicalId,
            value = recipes
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
                            KnowledgeDimensionCandidateType.RECIPE_GRAPH,
                        payload =
                            mapOf(
                                "recipes" to value
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