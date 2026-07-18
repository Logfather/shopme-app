package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.CreateFoodsJsonFromAgribalyse
import org.junit.Test

class CreateFoodsJsonFromAgribalyseTest {

    @Test
    fun createFoodsJsonFromAgribalyse() {
        CreateFoodsJsonFromAgribalyse.main(
            arrayOf(
                "../data/generated/foods.agribalyse.json"
            )
        )
    }
}