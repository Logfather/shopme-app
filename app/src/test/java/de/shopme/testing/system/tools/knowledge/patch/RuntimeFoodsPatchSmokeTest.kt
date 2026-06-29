package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.artifacts.ExistingFoodsKnowledgeLoader
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
import java.io.File
import java.time.Instant

class RuntimeFoodsPatchSmokeTest {

    @Test
    fun applyPatchToRuntimeFoodsKnowledge() {

        val input =
            File("src/main/assets/knowledge/runtime/foods.json")

        val existingEntries =
            ExistingFoodsKnowledgeLoader()
                .load(input)

        val existingCandidates =
            existingEntries.map { entry ->
                candidate(entry.normalizedName)
            }

        val smokeTestCandidate =
            candidate("__runtime_patch_smoke_test_food__")

        val patch =
            FoodsKnowledgePatch(
                metadata = FoodsKnowledgePatchMetadata(
                    source = "runtime_smoke_test",
                    generatedAt = Instant.EPOCH,
                    version = "1"
                ),
                entries = listOf(
                    FoodsKnowledgePatchEntry(
                        canonicalId = smokeTestCandidate.canonicalId,
                        candidate = smokeTestCandidate
                    )
                )
            )

        val result =
            DefaultFoodsPatchApplierFactory
                .create()
                .apply(
                    existingCandidates = existingCandidates,
                    patch = patch
                )

        FoodsPatchSummaryPrinter()
            .print(result)

        assertTrue(
            result.compileResult.validationResult.isValid
        )

        assertTrue(
            existingCandidates.none { candidate ->
                candidate.canonicalId == smokeTestCandidate.canonicalId
            }
        )

        assertEquals(
            1,
            result.diff.stats.addedCount
        )

        assertEquals(
            existingCandidates.size,
            result.stats.candidateCountBefore
        )

        assertEquals(
            1,
            result.stats.patchEntryCount
        )

        assertEquals(
            existingCandidates.size + 1,
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
                source = "runtime_smoke_test",
                sourceId = canonicalId,
                version = "1"
            )
        )
    }
}