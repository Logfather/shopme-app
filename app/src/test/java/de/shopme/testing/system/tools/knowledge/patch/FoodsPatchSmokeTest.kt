package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchApplierFactory
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.FoodsPatchSummaryPrinter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FoodsPatchSmokeTest {

    @Test
    fun applyPatchEndToEnd() {

        val applier =
            DefaultFoodsPatchApplierFactory.create()

        val printer =
            FoodsPatchSummaryPrinter()

        val apple =
            candidate("apple")

        val banana =
            candidate("banana")

        val patch =
            FoodsKnowledgePatch(
                metadata = FoodsKnowledgePatchMetadata(
                    source = "smoke_test",
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

        printer.print(result)

        assertTrue(
            result.compileResult.validationResult.isValid
        )

        assertEquals(
            listOf("apple", "banana"),
            result.candidates.map { candidate ->
                candidate.canonicalId
            }
        )

        assertEquals(
            1,
            result.stats.candidateCountBefore
        )

        assertEquals(
            1,
            result.stats.patchEntryCount
        )

        assertEquals(
            2,
            result.stats.candidateCountAfter
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
                source = "smoke_test",
                sourceId = canonicalId,
                version = "1"
            )
        )
    }
}