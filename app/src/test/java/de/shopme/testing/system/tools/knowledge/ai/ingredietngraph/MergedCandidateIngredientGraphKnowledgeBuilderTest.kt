package de.shopme.testing.system.tools.knowledge.ai.ingredientgraph

import de.shopme.tools.knowledge.ai.builder.ingredientgraph.MergedCandidateIngredientGraphKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateIngredientGraphKnowledgeBuilderTest {

    @Test
    fun buildsIngredientGraphKnowledgeFromCandidate() {
        val knowledge =
            MergedCandidateIngredientGraphKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "pizza",
                            ingredients =
                                listOf(
                                    "tomato",
                                    "cheese",
                                    "flour"
                                )
                        )
                    )
                )

        assertEquals(
            setOf(
                "cheese",
                "flour",
                "tomato"
            ),
            knowledge.entries["pizza"]?.ingredients
        )
    }

    @Test
    fun ignoresInvalidIngredientGraphValues() {
        val knowledge =
            MergedCandidateIngredientGraphKnowledgeBuilder()
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
    fun normalizesIngredients() {
        val knowledge =
            MergedCandidateIngredientGraphKnowledgeBuilder()
                .build(
                    listOf(
                        candidate(
                            canonicalId = "sauce",
                            ingredients =
                                listOf(
                                    " Tomato ",
                                    "tomato",
                                    "olive-oil",
                                    "olive_oil",
                                    ""
                                )
                        )
                    )
                )

        assertEquals(
            setOf(
                "olive oil",
                "tomato"
            ),
            knowledge.entries["sauce"]?.ingredients
        )
    }

    @Test
    fun sortsEntriesByCanonicalId() {
        val knowledge =
            MergedCandidateIngredientGraphKnowledgeBuilder()
                .build(
                    listOf(
                        candidate("pizza", listOf("tomato")),
                        candidate("cake", listOf("flour")),
                        candidate("salad", listOf("lettuce"))
                    )
                )

        assertEquals(
            listOf(
                "cake",
                "pizza",
                "salad"
            ),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        ingredients: List<String>
    ): CanonicalKnowledgeCandidate =
        rawCandidate(
            canonicalId = canonicalId,
            value = ingredients
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
                            KnowledgeDimensionCandidateType.INGREDIENT_GRAPH,
                        payload =
                            mapOf(
                                "ingredients" to value
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