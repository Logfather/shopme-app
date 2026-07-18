package de.shopme.testing.system.tools.knowledge.ai.packaging

import de.shopme.tools.knowledge.ai.builder.packaging.MergedCandidatePackagingKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MergedCandidatePackagingKnowledgeBuilderTest {

    private val builder =
        MergedCandidatePackagingKnowledgeBuilder()

    @Test
    fun buildsPackagingKnowledgeFromMergedCandidates() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "apple_juice",
                aliases = setOf("apple juice"),
                matchAliases = setOf("juice"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.PACKAGING,
                        payload = mapOf(
                            "packagingText" to "glass bottle",
                            "materials" to listOf("glass"),
                            "shapes" to listOf("bottle")
                        )
                    )
                ),
                metadata = testMetadata()
            )

        val knowledge =
            builder.build(listOf(candidate))

        val packaging =
            knowledge.entries["apple_juice"]

        assertNotNull(packaging)
        assertEquals(0.25, packaging.score)

        assertFalse(knowledge.entries.containsKey("apple juice"))
        assertFalse(knowledge.entries.containsKey("juice"))
    }

    @Test
    fun ignoresCandidatesWithoutPackaging() {
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
        val plastic =
            candidateWithPackaging(
                canonicalId = "plastic_water_bottle",
                material = "plastic"
            )

        val glass =
            candidateWithPackaging(
                canonicalId = "glass_juice_bottle",
                material = "glass"
            )

        val knowledge =
            builder.build(listOf(plastic, glass))

        assertEquals(
            listOf("glass_juice_bottle", "plastic_water_bottle"),
            knowledge.entries.keys.toList()
        )
    }

    @Test
    fun scoresPlasticHigherThanGlass() {
        val plastic =
            candidateWithPackaging(
                canonicalId = "plastic_water_bottle",
                material = "plastic"
            )

        val glass =
            candidateWithPackaging(
                canonicalId = "glass_juice_bottle",
                material = "glass"
            )

        val knowledge =
            builder.build(listOf(plastic, glass))

        val plasticScore =
            knowledge.entries["plastic_water_bottle"]!!.score

        val glassScore =
            knowledge.entries["glass_juice_bottle"]!!.score

        assertTrue(plasticScore > glassScore)
    }

    private fun candidateWithPackaging(
        canonicalId: String,
        material: String
    ): CanonicalKnowledgeCandidate {
        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            matchAliases = emptySet(),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.PACKAGING,
                    payload = mapOf(
                        "materials" to listOf(material)
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