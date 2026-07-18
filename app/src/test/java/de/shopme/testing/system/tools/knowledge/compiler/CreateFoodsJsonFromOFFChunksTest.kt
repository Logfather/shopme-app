package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.CreateFoodsJsonFromOFFChunks
import org.junit.Test

class CreateFoodsJsonFromOFFChunksTest {

    @Test
    fun createFoodsJsonFromOFFChunks() {
        CreateFoodsJsonFromOFFChunks.main(
            arrayOf(
                "../data/generated/openfoodfacts/chunks_10k",
                "../data/generated/openfoodfacts/catalog"
            )
        )
    }
}