package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.FoodsPatchMergeEngine
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class FoodsPatchMergeEngineTest {

    private val engine = FoodsPatchMergeEngine()

    @Test
    fun mergeKeepsExistingCandidateWhenPatchIsEmpty() {

        val existing = listOf(
            candidate("apple")
        )

        val patch = FoodsKnowledgePatch(
            metadata = patchMetadata(),
            entries = emptyList()
        )

        val result = engine.merge(
            existingCandidates = existing,
            patch = patch
        )

        assertEquals(existing, result)
    }

    @Test
    fun mergeReplacesCandidateWithSameCanonicalId() {

        val existing = listOf(
            candidate(
                canonicalId = "apple",
                aliases = setOf("old apple")
            )
        )

        val replacement = candidate(
            canonicalId = "apple",
            aliases = setOf("new apple")
        )

        val patch = FoodsKnowledgePatch(
            metadata = patchMetadata(),
            entries = listOf(
                FoodsKnowledgePatchEntry(
                    canonicalId = replacement.canonicalId,
                    candidate = replacement
                )
            )
        )

        val result = engine.merge(
            existingCandidates = existing,
            patch = patch
        )

        assertEquals(
            listOf(replacement),
            result
        )
    }

    @Test
    fun mergeAddsNewCandidate() {

        val existing = listOf(
            candidate("apple")
        )

        val banana = candidate("banana")

        val patch = FoodsKnowledgePatch(
            metadata = patchMetadata(),
            entries = listOf(
                FoodsKnowledgePatchEntry(
                    canonicalId = banana.canonicalId,
                    candidate = banana
                )
            )
        )

        val result = engine.merge(
            existingCandidates = existing,
            patch = patch
        )

        assertEquals(
            listOf(
                candidate("apple"),
                banana
            ),
            result
        )
    }

    @Test
    fun mergeReturnsCandidatesSortedByCanonicalId() {

        val existing = listOf(
            candidate("pear"),
            candidate("apple")
        )

        val banana = candidate("banana")

        val patch = FoodsKnowledgePatch(
            metadata = patchMetadata(),
            entries = listOf(
                FoodsKnowledgePatchEntry(
                    canonicalId = banana.canonicalId,
                    candidate = banana
                )
            )
        )

        val result = engine.merge(
            existingCandidates = existing,
            patch = patch
        )

        assertEquals(
            listOf(
                "apple",
                "banana",
                "pear"
            ),
            result.map { it.canonicalId }
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
                source = "test",
                sourceId = canonicalId,
                version = "1"
            )
        )
    }

    private fun patchMetadata(): FoodsKnowledgePatchMetadata {

        return FoodsKnowledgePatchMetadata(
            source = "test",
            generatedAt = Instant.EPOCH,
            version = "1"
        )
    }
}