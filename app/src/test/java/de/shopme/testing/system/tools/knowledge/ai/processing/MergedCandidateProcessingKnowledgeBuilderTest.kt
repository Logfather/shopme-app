package de.shopme.testing.system.tools.knowledge.ai.processing

import de.shopme.tools.knowledge.ai.builder.processing.MergedCandidateProcessingKnowledgeBuilder
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidate
import de.shopme.tools.knowledge.ki_candidates.KnowledgeDimensionCandidateType
import de.shopme.tools.knowledge.processing.ProcessingLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class MergedCandidateProcessingKnowledgeBuilderTest {

    @Test
    fun buildsProcessingKnowledgeFromNovaGroup() {
        val candidates =
            listOf(
                candidate(
                    canonicalId = "pizza",
                    novaGroup = 4
                )
            )

        val knowledge =
            MergedCandidateProcessingKnowledgeBuilder()
                .build(candidates)

        assertEquals(
            ProcessingLevel.NOVA_4,
            knowledge.entries["pizza"]
        )
    }

    @Test
    fun ignoresInvalidNovaGroups() {
        val candidates =
            listOf(
                candidate(
                    canonicalId = "unknown",
                    novaGroup = 9
                )
            )

        val knowledge =
            MergedCandidateProcessingKnowledgeBuilder()
                .build(candidates)

        assertFalse(
            knowledge.entries.containsKey("unknown")
        )
    }

    @Test
    fun keepsEntriesSortedByCanonicalId() {
        val candidates =
            listOf(
                candidate("yoghurt", 1),
                candidate("pizza", 4),
                candidate("bread", 2)
            )

        val knowledge =
            MergedCandidateProcessingKnowledgeBuilder()
                .build(candidates)

        assertEquals(
            listOf("bread", "pizza", "yoghurt"),
            knowledge.entries.keys.toList()
        )
    }

    private fun candidate(
        canonicalId: String,
        novaGroup: Int
    ): CanonicalKnowledgeCandidate =
        CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions = listOf(
                KnowledgeDimensionCandidate(
                    dimension = KnowledgeDimensionCandidateType.PROCESSING,
                    payload = mapOf(
                        "novaGroup" to novaGroup
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