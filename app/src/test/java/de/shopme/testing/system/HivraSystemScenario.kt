package de.shopme.testing.system

interface HivraSystemScenario {

    suspend fun run(
        context: HivraSystemTestContext
    )
}