package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.FoodsPatchCompiler
import de.shopme.tools.knowledge.patch.FoodsPatchMergeEngine
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationIssue
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationIssueCode
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationResult
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationSeverity
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FoodsPatchCompilerTest {

    @Test
    fun compileReturnsMergedCandidatesWhenPatchIsValid() {

        val compiler = FoodsPatchCompiler(
            validator = validValidator(),
            mergeEngine = FoodsPatchMergeEngine()
        )

        val existing = listOf(
            candidate("apple")
        )

        val banana = candidate("banana")

        val patch = patch(
            entries = listOf(
                FoodsKnowledgePatchEntry(
                    canonicalId = banana.canonicalId,
                    candidate = banana
                )
            )
        )

        val result = compiler.compile(
            existingCandidates = existing,
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
        assertTrue(
            result.validationResult.isValid
        )
    }

    @Test
    fun compileThrowsWhenPatchIsInvalid() {

        val compiler = FoodsPatchCompiler(
            validator = invalidValidator(),
            mergeEngine = FoodsPatchMergeEngine()
        )

        val patch = patch(
            entries = listOf(
                FoodsKnowledgePatchEntry(
                    canonicalId = "banana",
                    candidate = candidate("banana")
                )
            )
        )

        try {

            compiler.compile(
                existingCandidates = emptyList(),
                patch = patch
            )

            org.junit.Assert.fail("Expected IllegalStateException.")

        } catch (_: IllegalStateException) {
            // expected
        }
    }

    private fun validValidator(): FoodsPatchValidator {

        return object : FoodsPatchValidator {

            override fun validate(
                patch: FoodsKnowledgePatch
            ): FoodsPatchValidationResult {

                return FoodsPatchValidationResult(
                    issues = emptyList()
                )
            }
        }
    }

    private fun invalidValidator(): FoodsPatchValidator {

        return object : FoodsPatchValidator {

            override fun validate(
                patch: FoodsKnowledgePatch
            ): FoodsPatchValidationResult {

                return FoodsPatchValidationResult(
                    issues = listOf(
                        FoodsPatchValidationIssue(
                            code = FoodsPatchValidationIssueCode.DUPLICATE_CANONICAL_ID,
                            canonicalId = "banana",
                            severity = FoodsPatchValidationSeverity.ERROR,
                            message = "Invalid test patch."
                        )
                    )
                )
            }
        }
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
}