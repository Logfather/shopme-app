package de.shopme.testing.system.tools.knowledge.mapping.catalog.runner

import de.shopme.tools.knowledge.mapping.catalog.runner.PersistValidatedNutritionMappingsToCentralRepository
import kotlin.test.Test

class PersistRealValidatedNutritionMappingsToCentralRepositoryTest {

    @Test
    fun persistRealValidatedNutritionMappingsToCentralRepository() {

        PersistValidatedNutritionMappingsToCentralRepository.main(
            emptyArray()
        )
    }
}