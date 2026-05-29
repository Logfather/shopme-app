package de.shopme.testing.system.tests

import android.util.Log
import de.shopme.testing.system.scenario.HivraSystemScenario
import kotlinx.coroutines.runBlocking

class HivraSystemRunnerTest {

    fun runScenario(
        scenario: HivraSystemScenario,
        context: HivraSystemContextTest
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