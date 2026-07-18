package de.shopme.testing.system.tools.knowledge.ai.environment

import de.shopme.tools.knowledge.ai.builder.environment.MergedCandidateEnvironmentalImpactKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class MergedCandidateEnvironmentalImpactKnowledgeBuilderTest {

    private val builder =
        MergedCandidateEnvironmentalImpactKnowledgeBuilder()

    @Test
    fun buildsEnvironmentalImpactKnowledgeFromMergedCandidates() {
        val candidate = CanonicalKnowledgeCandidate(
            canonicalId = "broccoli",
            aliases = setOf("brocoli"),
            matchAliases = setOf("vegetable"),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT,
                    payload = mapOf(
                        "environmentScoreMptPerKg" to 0.0944,
                        "climateKgCo2EqPerKg" to 0.951,
                        "landUsePtPerKg" to 10.5,
                        "waterDeprivationM3PerKg" to 1.01
                    )
                )
            ),
            metadata = testMetadata()
        )

        val knowledge =
            builder.build(listOf(candidate))

        val broccoli =
            knowledge.entries["broccoli"]

        assertNotNull(broccoli)
        assertEquals(0.0944, broccoli.environmentScoreMptPerKg)
        assertEquals(0.951, broccoli.climateKgCo2EqPerKg)
        assertEquals(10.5, broccoli.landUsePtPerKg)
        assertEquals(1.01, broccoli.waterDeprivationM3PerKg)

        assertFalse(knowledge.entries.containsKey("brocoli"))
        assertFalse(knowledge.entries.containsKey("vegetable"))
    }

    @Test
    fun ignoresCandidatesWithoutEnvironmentalImpact() {
        val candidate = CanonicalKnowledgeCandidate(
            canonicalId = "apple",
            aliases = setOf("apfel"),
            matchAliases = setOf("fruit"),
            dimensions = emptyList(),
            metadata = testMetadata()
        )

        val knowledge =
            builder.build(listOf(candidate))

        assertEquals(emptyList(), knowledge.entries.keys.toList())
    }

    @Test
    fun producesStableSortedEntries() {
        val broccoli =
            candidateWithEnvironmentScore("broccoli", 0.0944)

        val apple =
            candidateWithEnvironmentScore("apple", 0.12)

        val knowledge =
            builder.build(listOf(broccoli, apple))

        assertEquals(
            listOf("apple", "broccoli"),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidateWithEnvironmentScore(
        canonicalId: String,
        score: Double
    ): CanonicalKnowledgeCandidate {
        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            matchAliases = emptySet(),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT,
                    payload = mapOf(
                        "environmentScoreMptPerKg" to score,
                        "climateKgCo2EqPerKg" to 0.0,
                        "landUsePtPerKg" to 0.0,
                        "waterDeprivationM3PerKg" to 0.0
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