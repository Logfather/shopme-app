package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.ki_candidates.CandidateMetadata
import de.shopme.tools.knowledge.ki_candidates.CanonicalKnowledgeCandidate
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchApplierFactory
import de.shopme.tools.knowledge.patch.DefaultFoodsPatchWriterFactory
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatch
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchEntry
import de.shopme.tools.knowledge.patch.FoodsKnowledgePatchMetadata
import de.shopme.tools.knowledge.patch.FoodsPatchSummaryPrinter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.time.Instant

class FoodsPatchWriteSmokeTest {

    @Test
    fun writePatchedFoodsKnowledge() {

        val existingCandidates =
            listOf(
                candidate("apple")
            )

        val patch =
            FoodsKnowledgePatch(
                metadata = FoodsKnowledgePatchMetadata(
                    source = "write_smoke_test",
                    generatedAt = Instant.EPOCH,
                    version = "1"
                ),
                entries = listOf(
                    FoodsKnowledgePatchEntry(
                        canonicalId = "banana",
                        candidate = candidate("banana")
                    )
                )
            )

        val applyResult =
            DefaultFoodsPatchApplierFactory
                .create()
                .apply(
                    existingCandidates = existingCandidates,
                    patch = patch
                )

        FoodsPatchSummaryPrinter()
            .print(applyResult)

        val output =
            File("data/generated/test-output/foods.json")

        val writeResult =
            DefaultFoodsPatchWriterFactory
                .create()
                .write(
                    result = applyResult,
                    outputFile = output.path
                )

        assertTrue(
            output.exists()
        )

        assertEquals(
            writeResult.serializedJson,
            output.readText()
        )

        assertEquals(
            2,
            writeResult.candidates.size
        )

        assertEquals(
            output.path,
            writeResult.stats.outputFile
        )

        assertEquals(
            2,
            writeResult.stats.candidateCount
        )

        assertEquals(
            applyResult,
            writeResult.applyResult
        )

        assertTrue(
            writeResult.candidates.any {
                it.canonicalId == "banana"
            }
        )

        output.delete()
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