package de.shopme.testing.system.tools.knowledge.ai.taxonomy

import de.shopme.tools.knowledge.ai.builder.taxonomy.MergedCandidateFoodTaxonomyKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MergedCandidateFoodTaxonomyKnowledgeBuilderTest {

    private val builder =
        MergedCandidateFoodTaxonomyKnowledgeBuilder()

    @Test
    fun buildsTaxonomyKnowledgeFromMainCategory() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "apple_juice",
                aliases = setOf("apple juice"),
                matchAliases = setOf("juice"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.TAXONOMY,
                        payload = mapOf(
                            "mainCategory" to "fruit juices"
                        )
                    )
                ),
                metadata = testMetadata()
            )

        val knowledge =
            builder.build(listOf(candidate))

        val taxonomy =
            knowledge.entries["apple_juice"]

        assertNotNull(taxonomy)
        assertEquals("fruit juices", taxonomy.parent)

        assertFalse(knowledge.entries.containsKey("apple juice"))
        assertFalse(knowledge.entries.containsKey("juice"))
    }

    @Test
    fun buildsTaxonomyKnowledgeFromHierarchyWhenMainCategoryIsMissing() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "apple_juice",
                aliases = emptySet(),
                matchAliases = emptySet(),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.TAXONOMY,
                        payload = mapOf(
                            "hierarchy" to listOf(
                                "beverages",
                                "fruit juices",
                                "apple juices"
                            )
                        )
                    )
                ),
                metadata = testMetadata()
            )

        val knowledge =
            builder.build(listOf(candidate))

        assertEquals(
            "apple juices",
            knowledge.entries["apple_juice"]?.parent
        )
    }

    @Test
    fun ignoresCandidatesWithoutTaxonomy() {
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
    fun producesStableSortedEntries() {
        val juice =
            candidateWithCategory(
                canonicalId = "apple_juice",
                category = "fruit juices"
            )

        val bread =
            candidateWithCategory(
                canonicalId = "bread",
                category = "breads"
            )

        val knowledge =
            builder.build(listOf(juice, bread))

        assertEquals(
            listOf("apple_juice", "bread"),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidateWithCategory(
        canonicalId: String,
        category: String
    ): CanonicalKnowledgeCandidate {
        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            matchAliases = emptySet(),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.TAXONOMY,
                    payload = mapOf(
                        "mainCategory" to category
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