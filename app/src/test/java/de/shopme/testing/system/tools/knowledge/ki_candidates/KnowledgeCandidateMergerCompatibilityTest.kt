package de.shopme.testing.system.tools.knowledge.ki_candidates

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeCandidateMerger
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals

class KnowledgeCandidateMergerCompatibilityTest {

    @Test
    fun doesNotMergeCandidatesFromDifferentProductFamilies() {

        val springOnion =
            CanonicalKnowledgeCandidate(
                canonicalId = "spring onion",
                aliases = setOf("spring onion"),
                matchAliases = setOf("aromatic plant"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.NUTRITION,
                        payload = mapOf("energyKcalPer100g" to 32.0)
                    )
                ),
                metadata = CandidateMetadata(
                    source = "open_food_facts",
                    sourceId = "off-spring-onion",
                    confidence = 1.0,
                    version = "1"
                )
            )

        val garlicPowder =
            CanonicalKnowledgeCandidate(
                canonicalId = "garlic powder",
                aliases = setOf("garlic powder"),
                matchAliases = setOf("aromatic plant"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT,
                        payload = mapOf("climateKgCo2EqPerKg" to 1.12)
                    )
                ),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = "agribalyse-garlic",
                    confidence = 1.0,
                    version = "3.2"
                )
            )

        val result =
            KnowledgeCandidateMerger()
                .merge(
                    listOf(
                        springOnion,
                        garlicPowder
                    )
                )

        assertEquals(2, result.candidates.size)
    }

    @Test
    fun mergesCandidatesFromSameProductFamily() {

        val off =
            CanonicalKnowledgeCandidate(
                canonicalId = "sunflower oil",
                aliases = setOf("pure sunflower oil"),
                matchAliases = setOf("vegetable oil"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.NUTRITION,
                        payload = mapOf("energyKcalPer100g" to 884.0)
                    )
                ),
                metadata = CandidateMetadata(
                    source = "open_food_facts",
                    sourceId = "off-sunflower-oil",
                    confidence = 1.0,
                    version = "1"
                )
            )

        val agribalyse =
            CanonicalKnowledgeCandidate(
                canonicalId = "huile de tournesol",
                aliases = setOf("sunflower oil"),
                matchAliases = setOf("vegetable oil"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT,
                        payload = mapOf("climateKgCo2EqPerKg" to 2.36)
                    )
                ),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = "agribalyse-sunflower-oil",
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
        assertEquals(2, result.candidates.single().dimensions.size)
    }
}