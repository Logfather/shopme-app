package de.shopme.testing.system.scenario

import de.shopme.testing.system.tests.HivraSystemContextTest

interface HivraSystemScenario {

    suspend fun run(
        context: HivraSystemContextTest
    )
}