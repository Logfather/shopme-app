package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.FoodsPatchDiffCalculator
import de.shopme.tools.knowledge.patch.FoodsPatchDiffOperation
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class FoodsPatchDiffCalculatorTest {

    private val calculator =
        FoodsPatchDiffCalculator()

    @Test
    fun newPatchEntryProducesAdd() {

        val diff =
            calculator.calculate(
                existingCandidates = listOf(
                    candidate("apple")
                ),
                patch = patch(
                    entries = listOf(
                        patchEntry("banana")
                    )
                )
            )

        assertEquals(
            FoodsPatchDiffOperation.ADD,
            diff.entries.first().operation
        )

        assertEquals(
            1,
            diff.stats.addedCount
        )

        assertEquals(
            0,
            diff.stats.updatedCount
        )

        assertEquals(
            0,
            diff.stats.unchangedCount
        )
    }

    @Test
    fun existingPatchEntryProducesUpdate() {

        val diff =
            calculator.calculate(
                existingCandidates = listOf(
                    candidate("banana")
                ),
                patch = patch(
                    entries = listOf(
                        patchEntry("banana")
                    )
                )
            )

        assertEquals(
            FoodsPatchDiffOperation.UPDATE,
            diff.entries.first().operation
        )

        assertEquals(
            0,
            diff.stats.addedCount
        )

        assertEquals(
            1,
            diff.stats.updatedCount
        )

        assertEquals(
            0,
            diff.stats.unchangedCount
        )
    }

    @Test
    fun diffEntriesAreSortedByCanonicalId() {

        val diff =
            calculator.calculate(
                existingCandidates = emptyList(),
                patch = patch(
                    entries = listOf(
                        patchEntry("pear"),
                        patchEntry("apple"),
                        patchEntry("banana")
                    )
                )
            )

        assertEquals(
            listOf(
                "apple",
                "banana",
                "pear"
            ),
            diff.entries.map { entry ->
                entry.canonicalId
            }
        )

        assertEquals(
            3,
            diff.stats.addedCount
        )

        assertEquals(
            0,
            diff.stats.updatedCount
        )

        assertEquals(
            0,
            diff.stats.unchangedCount
        )
    }

    private fun patchEntry(
        canonicalId: String
    ): FoodsKnowledgePatchEntry {

        val candidate =
            candidate(canonicalId)

        return FoodsKnowledgePatchEntry(
            canonicalId = canonicalId,
            candidate = candidate
        )
    }

    private fun patch(
        entries: List<FoodsKnowledgePatchEntry>
    ): FoodsKnowledgePatch {

        return FoodsKnowledgePatch(
            metadata = FoodsKnowledgePatchMetadata(
                source = "test",
                generatedAt = Instant.EPOCH,
                version = "1"
            ),
            entries = entries
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