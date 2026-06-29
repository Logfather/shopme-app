package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.artifacts.ExistingFoodsKnowledgeLoader
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchApplierFactory
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.FoodsPatchDiffOperation
import de.shopme.tools.knowledge.patch.FoodsPatchSummaryPrinter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Instant

class RuntimeFoodsPatchUpdateSmokeTest {

    @Test
    fun applyUpdatePatchToRuntimeFoodsKnowledge() {

        val input =
            File("src/main/assets/knowledge/runtime/foods.json")

        val existingEntries =
            ExistingFoodsKnowledgeLoader()
                .load(input)

        val existingCandidates =
            existingEntries.map { entry ->
                candidate(entry.normalizedName)
            }

        val targetCanonicalId =
            existingCandidates.first().canonicalId

        assertTrue(
            targetCanonicalId.isNotBlank()
        )

        assertTrue(
            existingCandidates.any { candidate ->
                candidate.canonicalId == targetCanonicalId
            }
        )

        val updatedCandidate =
            candidate(
                canonicalId = targetCanonicalId,
                aliases = setOf("updated apple smoke test")
            )

        val patch =
            FoodsKnowledgePatch(
                metadata = FoodsKnowledgePatchMetadata(
                    source = "runtime_update_smoke_test",
                    generatedAt = Instant.EPOCH,
                    version = "1"
                ),
                entries = listOf(
                    FoodsKnowledgePatchEntry(
                        canonicalId = updatedCandidate.canonicalId,
                        candidate = updatedCandidate
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

        assertEquals(
            existingCandidates.size,
            result.stats.candidateCountBefore
        )

        assertEquals(
            1,
            result.stats.patchEntryCount
        )

        assertEquals(
            existingCandidates.size,
            result.stats.candidateCountAfter
        )

        assertEquals(
            0,
            result.diff.stats.addedCount
        )

        assertEquals(
            1,
            result.diff.stats.updatedCount
        )

        assertEquals(
            0,
            result.diff.stats.unchangedCount
        )

        assertEquals(
            1,
            result.diff.entries.size
        )

        assertEquals(
            targetCanonicalId,
            result.diff.entries.first().canonicalId
        )

        assertEquals(
            FoodsPatchDiffOperation.UPDATE,
            result.diff.entries.first().operation
        )
    }

    private fun candidate(
        canonicalId: String,
        aliases: Set<String> = emptySet()
    ): CanonicalKnowledgeCandidate {

        return CanonicalKnowledgeCandidate(
            canonicalId = canonicalId,
            aliases = aliases,
            dimensions = emptyList(),
            metadata = CandidateMetadata(
                confidence = 1.0,
                source = "runtime_update_smoke_test",
                sourceId = canonicalId,
                version = "1"
            )
        )
    }
}