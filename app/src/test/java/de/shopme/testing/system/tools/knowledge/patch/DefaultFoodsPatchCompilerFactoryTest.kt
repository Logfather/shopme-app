package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchCompilerFactory
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DefaultFoodsPatchCompilerFactoryTest {

    @Test
    fun factoryCreatesCompilerThatCanApplyValidPatch() {

        val compiler =
            DefaultFoodsPatchCompilerFactory.create()

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
            compiler.compile(
                existingCandidates = listOf(apple),
                patch = patch
            )

        assertEquals(
            listOf("apple", "banana"),
            result.candidates.map { candidate ->
                candidate.canonicalId
            }
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