package de.shopme.testing.system.test

import android.util.Log
import de.shopme.testing.system.scenario.HivraSystemScenario
import kotlinx.coroutines.runBlocking

class HivraSystemTestRunner {

    fun runScenario(
        scenario: HivraSystemScenario,
        context: HivraSystemTestContext
    ) {

        Log.d(
            "HIVRA_TEST",
            "================================================"
        )

        Log.d(
            "HIVRA_TEST",
            "Starting scenario: ${scenario::class.simpleName}"
        )

        runBlocking {

            scenario.run(context)
        }

        Log.d(
            "HIVRA_TEST",
            "Finished scenario: ${scenario::class.simpleName}"
        )

        Log.d(
            "HIVRA_TEST",
            "================================================"
        )
    }
}