package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.AnalyzeFoodsCatalogSize
import org.junit.Test

class AnalyzeFoodsCatalogSizeTest {

    @Test
    fun analyzeFoodsCatalogSize() {
        AnalyzeFoodsCatalogSize.main(
            arrayOf(
                "../data/generated/foods.off.json",
                "1000"
            )
        )
    }
}