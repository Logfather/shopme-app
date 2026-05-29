package de.shopme.testing.system

import android.util.Log
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