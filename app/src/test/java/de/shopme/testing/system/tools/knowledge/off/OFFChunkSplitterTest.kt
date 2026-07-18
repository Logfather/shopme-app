package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.off.loader.OpenFoodFactsChunkSplitter
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class OFFChunkSplitterTest {

    @Test
    fun splitSlimOpenFoodFactsDumpIntoChunks() {
        val input =
            File("../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz")

        val outputDirectory =
            File("../data/generated/openfoodfacts/chunks_10k")

        val result =
            OpenFoodFactsChunkSplitter().split(
                inputFile = input,
                outputDirectory = outputDirectory,
                recordsPerChunk = 10_000
            )

        assertTrue(result.totalRecords > 0)
        assertTrue(result.chunkCount > 0)
        assertTrue(outputDirectory.listFiles().orEmpty().isNotEmpty())
    }
}