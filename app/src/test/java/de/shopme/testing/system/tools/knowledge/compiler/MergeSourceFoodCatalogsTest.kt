package de.shopme.testing.system.tools.knowledge.compiler

import de.shopme.tools.knowledge.compiler.MergeSourceFoodCatalogs
import org.junit.Test

class MergeSourceFoodCatalogsTest {

    @Test
    fun mergeSourceFoodCatalogs() {
        MergeSourceFoodCatalogs.main(
            arrayOf(
                "../data/generated/foods.off.json",
                "../data/generated/foods.agribalyse.json",
                "../data/generated/foods.complete.json"
            )
        )
    }
}