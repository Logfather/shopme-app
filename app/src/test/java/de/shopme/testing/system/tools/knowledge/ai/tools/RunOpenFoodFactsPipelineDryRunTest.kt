package de.shopme.testing.system.tools.knowledge.ai.tools

import de.shopme.tools.knowledge.ai.tools.RunOpenFoodFactsPipelineDryRun
import org.junit.Test

class RunOpenFoodFactsPipelineDryRunTest {

    @Test
    fun dryRunExecutesPipelineWithoutException() {

        println(">>> Starting OFF Dry Run Test")

        RunOpenFoodFactsPipelineDryRun.main(
            arrayOf("50000")
        )

        println(">>> OFF Dry Run finished")
    }
}