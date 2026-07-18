package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.off.loader.OpenFoodFactsDumpReader
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals

class OFFSlimDumpValidationTest {

    @Test
    fun slimDumpContainsSameNumberOfProductsAsOriginalDump() {

        val reader = OpenFoodFactsDumpReader()

        val original =
            File("../data/raw/openfoodfacts/openfoodfacts-products.jsonl.gz")

        val slim =
            File("../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz")

        val originalCount =
            reader.countLines(original)

        val slimCount =
            reader.countLines(slim)

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF SLIM VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Original : $originalCount")
        println("Slim     : $slimCount")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        assertEquals(originalCount, slimCount)
    }
}