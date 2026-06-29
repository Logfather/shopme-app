package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.validation.DefaultFoodsPatchValidator
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationIssueCode
import de.shopme.tools.knowledge.patch.validation.FoodsPatchValidationSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class FoodsPatchValidatorTest {

    private val validator = DefaultFoodsPatchValidator()

    @Test
    fun duplicateCanonicalIdsProduceValidationError() {

        val patch =
            patch(
                entries = listOf(
                    patchEntry("apple"),
                    patchEntry("banana"),
                    patchEntry("banana")
                )
            )

        val result =
            validator.validate(patch)

        assertFalse(result.isValid)

        assertEquals(
            1,
            result.issues.size
        )

        assertEquals(
            "banana",
            result.issues.first().canonicalId
        )

        assertEquals(
            FoodsPatchValidationSeverity.ERROR,
            result.issues.first().severity
        )

        assertEquals(
            FoodsPatchValidationIssueCode.DUPLICATE_CANONICAL_ID,
            result.issues.first().code
        )
    }

    @Test
    fun uniqueCanonicalIdsProduceNoValidationErrors() {

        val patch =
            patch(
                entries = listOf(
                    patchEntry("apple"),
                    patchEntry("banana"),
                    patchEntry("pear")
                )
            )

        val result =
            validator.validate(patch)

        assertTrue(result.isValid)
        assertTrue(result.issues.isEmpty())
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

    @Test
    fun mismatchingCanonicalIdsProduceValidationError() {

        val candidate =
            candidate("banana")

        val patch =
            patch(
                entries = listOf(
                    FoodsKnowledgePatchEntry(
                        canonicalId = "apple",
                        candidate = candidate
                    )
                )
            )

        val result =
            validator.validate(patch)

        assertFalse(result.isValid)

        assertEquals(
            1,
            result.issues.size
        )

        assertEquals(
            "apple",
            result.issues.first().canonicalId
        )

        assertEquals(
            FoodsPatchValidationSeverity.ERROR,
            result.issues.first().severity
        )

        assertEquals(
            FoodsPatchValidationIssueCode.CANONICAL_ID_MISMATCH,
            result.issues.first().code
        )
    }

    @Test
    fun blankCanonicalIdProducesValidationError() {

        val patch =
            patch(
                entries = listOf(
                    FoodsKnowledgePatchEntry(
                        canonicalId = "",
                        candidate = candidate("apple")
                    )
                )
            )

        val result =
            validator.validate(patch)

        assertFalse(result.isValid)

        val blankIssue =
            result.issues.first { issue ->
                issue.code == FoodsPatchValidationIssueCode.BLANK_CANONICAL_ID
            }

        assertEquals(
            FoodsPatchValidationSeverity.ERROR,
            blankIssue.severity
        )

        assertEquals(
            "",
            blankIssue.canonicalId
        )
    }

    @Test
    fun blankPatchMetadataSourceProducesValidationError() {

        val patch = FoodsKnowledgePatch(
            metadata = FoodsKnowledgePatchMetadata(
                source = "",
                generatedAt = Instant.EPOCH,
                version = "1"
            ),
            entries = emptyList()
        )

        val result = validator.validate(patch)

        assertFalse(result.isValid)

        val issue = result.issues.first { issue ->
            issue.code == FoodsPatchValidationIssueCode.BLANK_PATCH_SOURCE
        }

        assertEquals(
            "<patch>",
            issue.canonicalId
        )

        assertEquals(
            FoodsPatchValidationSeverity.ERROR,
            issue.severity
        )
    }

    @Test
    fun blankPatchMetadataVersionProducesValidationError() {

        val patch = FoodsKnowledgePatch(
            metadata = FoodsKnowledgePatchMetadata(
                source = "test",
                generatedAt = Instant.EPOCH,
                version = ""
            ),
            entries = emptyList()
        )

        val result = validator.validate(patch)

        assertFalse(result.isValid)

        val issue = result.issues.first { issue ->
            issue.code == FoodsPatchValidationIssueCode.BLANK_PATCH_VERSION
        }

        assertEquals(
            "<patch>",
            issue.canonicalId
        )

        assertEquals(
            FoodsPatchValidationSeverity.ERROR,
            issue.severity
        )
    }
}