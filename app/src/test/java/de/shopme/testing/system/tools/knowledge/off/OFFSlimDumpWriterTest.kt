package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.off.loader.OFFSlimDumpWriter
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class OFFSlimDumpWriterTest {

    @Test
    fun writeSlimDump_createsSlimOpenFoodFactsDump() {
        val result =
            OFFSlimDumpWriter().writeSlimDump(
                inputFile = File("../data/raw/openfoodfacts/openfoodfacts-products.jsonl.gz"),
                outputFile = File("../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz")
            )

        assertTrue(result.read > 0)
        assertTrue(result.written > 0)
        assertTrue(result.outputFile.exists())
    }
}