package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.artifacts.FoodsKnowledgeCandidateSerializer
import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchWriter
import de.shopme.tools.knowledge.patch.FoodsPatchApplyResult
import de.shopme.tools.knowledge.patch.FoodsPatchApplyStats
import de.shopme.tools.knowledge.patch.FoodsPatchCompileResult
import de.shopme.tools.knowledge.patch.FoodsPatchDiff
import de.shopme.tools.knowledge.patch.FoodsPatchDiffStats
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodsPatchWriterTest {

    @Test
    fun writeReturnsWriteResult() {

        val writer =
            DefaultFoodsPatchWriter(
                serializer = FoodsKnowledgeCandidateSerializer()
            )

        val candidates =
            listOf(
                candidate("apple"),
                candidate("banana")
            )

        val applyResult =
            FoodsPatchApplyResult(
                candidates = candidates,
                compileResult = FoodsPatchCompileResult(
                    candidates = candidates,
                    validationResult = FoodsPatchValidationResult(
                        issues = emptyList()
                    )
                ),
                diff = FoodsPatchDiff(
                    entries = emptyList(),
                    stats = FoodsPatchDiffStats(
                        addedCount = 0,
                        updatedCount = 0,
                        unchangedCount = 0
                    )
                ),
                stats = FoodsPatchApplyStats(
                    candidateCountBefore = 1,
                    candidateCountAfter = 2,
                    patchEntryCount = 1
                )
            )

        val result =
            writer.write(
                result = applyResult,
                outputFile = "test-output.json"
            )

        assertEquals(
            candidates,
            result.candidates
        )

        assertEquals(
            applyResult,
            result.applyResult
        )

        assertEquals(
            2,
            result.stats.candidateCount
        )

        assertEquals(
            "test-output.json",
            result.stats.outputFile
        )

        assertTrue(
            result.serializedJson.contains(
                "\"id\": \"apple\""
            )
        )

        assertTrue(
            result.serializedJson.contains(
                "\"id\": \"banana\""
            )
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