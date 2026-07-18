package de.shopme.testing.system.tools.knowledge.ki_candidates

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.normalizer.KnowledgeCandidateNormalizer
import kotlin.test.Test
import kotlin.test.assertTrue

class KnowledgeCandidateNormalizerTest {

    @Test
    fun addsSimplifiedAliasVariants() {
        val candidate =
            CanonicalKnowledgeCandidate(
                canonicalId = "cannelle, poudre",
                aliases = setOf("cinnamon, powder"),
                dimensions = emptyList(),
                metadata = CandidateMetadata(
                    source = "agribalyse",
                    sourceId = "1",
                    confidence = 1.0,
                    version = "test"
                )
            )

        val normalized =
            KnowledgeCandidateNormalizer()
                .normalize(listOf(candidate))
                .single()

        assertTrue(normalized.aliases.contains("cinnamon"))
        assertTrue(normalized.aliases.contains("powder cinnamon"))
        assertTrue(normalized.aliases.contains("cannelle, poudre"))
    }
}