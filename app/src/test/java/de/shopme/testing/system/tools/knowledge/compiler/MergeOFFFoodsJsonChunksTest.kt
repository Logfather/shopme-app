package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.MergeOFFFoodsJsonChunks
import org.junit.Test

class MergeOFFFoodsJsonChunksTest {

    @Test
    fun mergeOFFFoodsJsonChunks() {
        MergeOFFFoodsJsonChunks.main(
            arrayOf(
                "../data/generated/openfoodfacts/catalog",
                "../data/generated/foods.off.json"
            )
        )
    }
}