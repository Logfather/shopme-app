package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchApplierFactory
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.FoodsPatchDiffOperation
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DefaultFoodsPatchApplierFactoryTest {

    @Test
    fun factoryCreatesApplierThatCanApplyValidPatch() {

        val applier =
            DefaultFoodsPatchApplierFactory.create()

        val apple =
            candidate("apple")

        val banana =
            candidate("banana")

        val patch =
            FoodsKnowledgePatch(
                metadata = FoodsKnowledgePatchMetadata(
                    source = "test",
                    generatedAt = Instant.EPOCH,
                    version = "1"
                ),
                entries = listOf(
                    FoodsKnowledgePatchEntry(
                        canonicalId = banana.canonicalId,
                        candidate = banana
                    )
                )
            )

        val result =
            applier.apply(
                existingCandidates = listOf(apple),
                patch = patch
            )

        assertEquals(
            listOf(
                "apple",
                "banana"
            ),
            result.candidates.map { candidate ->
                candidate.canonicalId
            }
        )

        assertEquals(
            1,
            result.stats.candidateCountBefore
        )

        assertEquals(
            2,
            result.stats.candidateCountAfter
        )

        assertEquals(
            1,
            result.stats.patchEntryCount
        )

        assertEquals(
            1,
            result.diff.entries.size
        )

        assertEquals(
            FoodsPatchDiffOperation.ADD,
            result.diff.entries.first().operation
        )
    }

    private fun candidate(
        canonicalId: String
    ): CanonicalKnowledgeCandidate {

        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = emptySet(),
            dimensions = emptyList(),
            metadata = CandidateMetadata(
                confidence = 1.0,
                source = "test",
                sourceId = canonicalId,
                version = "1"
            )
        )
    }
}