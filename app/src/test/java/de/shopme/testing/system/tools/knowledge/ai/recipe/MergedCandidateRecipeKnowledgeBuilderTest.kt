package de.shopme.testing.system.tools.knowledge.ai.recipe

import de.shopme.tools.knowledge.ai.builder.recipe.MergedCandidateRecipeKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateRecipeKnowledgeBuilderTest {

    @Test
    fun buildsRecipeKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateRecipeKnowledgeBuilder()
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
            listOf(
                "bruschetta",
                "tomato_sauce"
            ),
            knowledge.entries["tomato"]
        )
    }

    @Test
    fun ignoresInvalidRecipeValues() {
        val knowledge =
            MergedCandidateRecipeKnowledgeBuilder()
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
    fun normalizesRecipes() {
        val knowledge =
            MergedCandidateRecipeKnowledgeBuilder()
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
            listOf(
                "apple_crumble",
                "apple_pie"
            ),
            knowledge.entries["apple"]
        )
    }

    @Test
    fun sortsEntriesByCanonicalId() {
        val knowledge =
            MergedCandidateRecipeKnowledgeBuilder()
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
                            KnowledgeDimensionCandidateType.RECIPE,
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