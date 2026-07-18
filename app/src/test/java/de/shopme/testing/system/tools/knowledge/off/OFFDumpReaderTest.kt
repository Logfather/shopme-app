package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.off.loader.OpenFoodFactsDumpReader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OFFDumpReaderTest {

    @Test
    fun readLines_readsFirstRecordsFromCompressedDump() {
        val file =
            File("../data/raw/openfoodfacts/openfoodfacts-products.jsonl.gz")

        val lines =
            OpenFoodFactsDumpReader()
                .readLines(
                    file = file,
                    maxRecords = 5
                )

        assertEquals(5, lines.size)

        assertTrue(
            lines.all { it.isNotBlank() }
        )
    }
}