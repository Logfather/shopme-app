package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.CreateRuntimeFoodsJson
import org.junit.Test

class CreateRuntimeFoodsJsonTest {

    @Test
    fun createRuntimeFoodsJson() {
        CreateRuntimeFoodsJson.main(
            arrayOf(
                "../data/generated/foods.complete.json",
                "../data/raw/catalog/supermarket_dataset.translated.json",
                "../data/generated/foods.runtime.json"
            )
        )
    }
}