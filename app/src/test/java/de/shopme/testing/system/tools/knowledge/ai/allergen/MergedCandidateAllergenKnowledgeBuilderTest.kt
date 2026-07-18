package de.shopme.testing.system.tools.knowledge.ai.allergen

import de.shopme.tools.knowledge.ai.builder.allergen.MergedCandidateAllergenKnowledgeBuilder
import de.shopme.tools.knowledge.allergen.Allergen
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MergedCandidateAllergenKnowledgeBuilderTest {

    private val builder =
        MergedCandidateAllergenKnowledgeBuilder()

    @Test
    fun buildsAllergenKnowledgeFromMergedCandidates() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "milk_chocolate",
                aliases = setOf("milk chocolate"),
                matchAliases = setOf("chocolate"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.ALLERGENS,
                        payload = mapOf(
                            "allergens" to listOf("milk")
                        )
                    )
                ),
                metadata = testMetadata()
            )

        val knowledge =
            builder.build(listOf(candidate))

        assertEquals(
            setOf(Allergen.MILK),
            knowledge.entries["milk_chocolate"]
        )

        assertFalse(knowledge.entries.containsKey("milk chocolate"))
        assertFalse(knowledge.entries.containsKey("chocolate"))
    }

    @Test
    fun ignoresCandidatesWithoutAllergens() {
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

        assertTrue(knowledge.entries.isEmpty())
    }

    @Test
    fun ignoresTraceOnlyPayloads() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "dark_chocolate",
                aliases = emptySet(),
                matchAliases = emptySet(),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.ALLERGENS,
                        payload = mapOf(
                            "traces" to listOf("nuts")
                        )
                    )
                ),
                metadata = testMetadata()
            )

        val knowledge =
            builder.build(listOf(candidate))

        assertTrue(knowledge.entries.isEmpty())
    }

    @Test
    fun producesStableSortedEntries() {
        val milk =
            candidateWithAllergen(
                canonicalId = "milk_chocolate",
                allergen = "milk"
            )

        val gluten =
            candidateWithAllergen(
                canonicalId = "wheat_bread",
                allergen = "gluten"
            )

        val knowledge =
            builder.build(listOf(gluten, milk))

        assertEquals(
            listOf("milk_chocolate", "wheat_bread"),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidateWithAllergen(
        canonicalId: String,
        allergen: String
    ): CanonicalKnowledgeCandidate {
        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            matchAliases = emptySet(),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.ALLERGENS,
                    payload = mapOf(
                        "allergens" to listOf(allergen)
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