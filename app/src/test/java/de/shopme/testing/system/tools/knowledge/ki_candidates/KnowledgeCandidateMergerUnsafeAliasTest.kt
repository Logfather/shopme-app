package de.shopme.testing.system.tools.knowledge.ki_candidates

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeCandidateMerger
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import kotlin.test.Test
import kotlin.test.assertEquals

class KnowledgeCandidateMergerUnsafeAliasTest {

    @Test
    fun doesNotMergeCandidatesOnlyBecauseOfGenericAlias() {

        val vinegar =
            CanonicalKnowledgeCandidate(
                canonicalId = "vinegar",
                aliases = setOf("vinegar"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.NUTRITION,
                        payload = mapOf(
                            "energyKcalPer100g" to 20.0
                        )
                    )
                ),
                metadata = CandidateMetadata(
                    source = "open_food_facts",
                    sourceId = "off-vinegar",
                    confidence = 1.0,
                    version = "1"
                )
            )

        val balsamic =
            CanonicalKnowledgeCandidate(
                canonicalId = "vinaigre balsamique",
                aliases = setOf("vinegar"),
                dimensions = listOf(
                    KnowledgeDimensionCandidate(
                        dimension = KnowledgeDimensionCandidateType.ENVIRONMENTAL_IMPACT,
                        payload = mapOf(
                            "climateKgCo2EqPerKg" to 0.96
                        )
                    )
                ),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = "agribalyse-balsamic-vinegar",
                    confidence = 1.0,
                    version = "3.2"
                )
            )

        val result =
            KnowledgeCandidateMerger()
                .merge(
                    listOf(
                        vinegar,
                        balsamic
                    )
                )

        assertEquals(
            2,
            result.candidates.size
        )
    }
}