package de.shopme.testing.system.tools.knowledge.ai.ingredients

import de.shopme.tools.knowledge.ai.builder.ingredients.MergedCandidateIngredientsKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class MergedCandidateIngredientsKnowledgeBuilderTest {

    private val builder =
        MergedCandidateIngredientsKnowledgeBuilder()

    @Test
    fun buildsIngredientsKnowledgeFromMergedCandidates() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "banana_bread",
                aliases = setOf("banana bread"),
                matchAliases = setOf("bakery"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.INGREDIENTS,
                        payload = mapOf(
                            "ingredientsText" to "Banana, Flour, Sugar",
                            "ingredients" to listOf("banana", "flour", "sugar")
                        )
                    )
                ),
                metadata = testMetadata()
            )

        val knowledge =
            builder.build(listOf(candidate))

        val facts =
            knowledge.entries["banana_bread"]

        assertNotNull(facts)
        val ingredients =
            knowledge.entries["banana_bread"]

        assertNotNull(ingredients)

        assertEquals(
            setOf("banana", "flour", "sugar"),
            ingredients
        )

        assertFalse(knowledge.entries.containsKey("banana bread"))
        assertFalse(knowledge.entries.containsKey("bakery"))
    }

    @Test
    fun ignoresCandidatesWithoutIngredients() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "apple",
                aliases = emptySet(),
                matchAliases = emptySet(),
                dimensions = emptyList(),
                metadata = testMetadata()
            )

        val knowledge =
            builder.build(listOf(candidate))

        assertEquals(emptyList(), knowledge.entries.keys.toList())
    }

    @Test
    fun producesStableSortedEntries() {
        val banana =
            candidateWithIngredients("banana")

        val apple =
            candidateWithIngredients("apple")

        val knowledge =
            builder.build(listOf(banana, apple))

        assertEquals(
            listOf("apple", "banana"),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidateWithIngredients(
        canonicalId: String
    ): CanonicalKnowledgeCandidate {
        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            matchAliases = emptySet(),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.INGREDIENTS,
                    payload = mapOf(
                        "ingredients" to listOf(canonicalId)
                    )
                )
            ),
            metadata = testMetadata()
        )
    }

    private fun testMetadata(): CandidateMetadata {
        return CandidateMetadata(
            source = "test",
            sourceId = "test",
            version = "test",
            confidence = 1.0
        )
    }
}