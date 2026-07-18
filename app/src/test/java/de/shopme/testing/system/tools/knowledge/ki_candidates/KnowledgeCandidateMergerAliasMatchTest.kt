package de.shopme.testing.system.tools.knowledge.ki_candidates

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeCandidateMerger
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KnowledgeCandidateMergerAliasMatchTest {

    @Test
    fun mergesCandidatesWhenAliasesOverlap() {

        val off =
            CanonicalKnowledgeCandidate(
                canonicalId = "sunflower oil",
                aliases = setOf("sunflower oil"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.NUTRITION,
                        payload = mapOf(
                            "energyKcalPer100g" to 66.0
                        )
                    )
                ),
                metadata = CandidateMetadata(
                    source = "open_food_facts",
                    sourceId = "off-mustard",
                    confidence = 1.0,
                    version = "1"
                )
            )

        val agribalyse =
            CanonicalKnowledgeCandidate(
                canonicalId = "huile de tournesol",
                aliases = setOf("huile de tournesol", "sunflower oil"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT,
                        payload = mapOf(
                            "climateKgCo2EqPerKg" to 1.2
                        )
                    )
                ),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = "agribalyse-moutarde",
                    confidence = 1.0,
                    version = "3.2"
                )
            )

        val result =
            KnowledgeCandidateMerger()
                .merge(
                    listOf(
                        off,
                        agribalyse
                    )
                )

        assertEquals(1, result.candidates.size)

        val merged =
            result.candidates.single()

        assertTrue(merged.aliases.contains("sunflower oil"))
        assertTrue(merged.aliases.contains("huile de tournesol"))

        assertTrue(
            merged.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.NUTRITION
            }
        )

        assertTrue(
            merged.dimensions.any {
                it.dimension == KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT
            }
        )
    }
}