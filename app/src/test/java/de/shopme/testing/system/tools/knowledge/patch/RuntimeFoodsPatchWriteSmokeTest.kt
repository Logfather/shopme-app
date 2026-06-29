package de.shopme.testing.system.tools.knowledge.patch

import de.shopme.tools.knowledge.artifacts.ExistingFoodsKnowledgeLoader
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

class RuntimeFoodsPatchWriteSmokeTest {

    @Test
    fun writePatchedRuntimeFoodsKnowledge() {

        val runtimeFoods =
            File(
                "src/main/assets/knowledge/runtime/foods.json"
            )

        val existingEntries =
            ExistingFoodsKnowledgeLoader()
                .load(runtimeFoods)

        val existingCandidates =
            existingEntries.map { entry ->

                CanonicalKnowledgeCandidate(
                    canonicalId = entry.normalizedName,
                    aliases = emptySet(),
                    dimensions = emptyList(),
                    metadata = CandidateMetadata(
                        confidence = 1.0,
                        source = "runtime",
                        sourceId = entry.normalizedName,
                        version = "1"
                    )
                )
            }

        val patch =
            FoodsKnowledgePatch(
                metadata = FoodsKnowledgePatchMetadata(
                    source = "runtime_write_smoke_test",
                    generatedAt = Instant.EPOCH,
                    version = "1"
                ),
                entries = listOf(
                    FoodsKnowledgePatchEntry(
                        canonicalId = "__runtime_write_smoke_test_food__",
                        candidate = candidate(
                            "__runtime_write_smoke_test_food__"
                        )
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
            File(
                "build/runtime-foods-write-smoke-test.json"
            )

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
            applyResult.candidates.size,
            writeResult.stats.candidateCount
        )

        assertEquals(
            output.path,
            writeResult.stats.outputFile
        )

        assertEquals(
            writeResult.serializedJson,
            output.readText()
        )

        assertTrue(
            output.readText().contains(
                "__runtime_write_smoke_test_food__"
            )
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