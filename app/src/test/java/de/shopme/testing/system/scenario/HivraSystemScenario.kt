package de.shopme.testing.system.scenario

import de.shopme.testing.system.test.HivraSystemTestContext

interface HivraSystemScenario {

    suspend fun run(
        context: HivraSystemTestContext
    )
}