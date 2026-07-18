package de.shopme.testing.system.tools.knowledge.mapping.catalog.runner

import de.shopme.tools.knowledge.mapping.catalog.runner.WriteValidatedNutritionCatalogServerMappings
import kotlin.test.Test

class RunWriteValidatedNutritionCatalogServerMappingsTest {

    @Test
    fun runWriteValidatedNutritionCatalogServerMappings() {

        WriteValidatedNutritionCatalogServerMappings.main(
            emptyArray()
        )
    }
}