package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.CreateFoodsJsonFromOFF
import org.junit.Test

class CreateFoodsJsonFromOFFTest {

    @Test
    fun createFoodsJsonFromOFF() {
        CreateFoodsJsonFromOFF.main(
            arrayOf(
                "../data/generated/foods.off.json"
            )
        )
    }
}