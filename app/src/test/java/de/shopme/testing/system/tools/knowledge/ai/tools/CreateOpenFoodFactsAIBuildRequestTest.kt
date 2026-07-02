package de.shopme.testing.system.tools.knowledge.ai.tools

import de.shopme.tools.knowledge.ai.tools.CreateOpenFoodFactsAIBuildRequest
import org.junit.Test

class CreateOpenFoodFactsAIBuildRequestTest {

    @Test
    fun createBuildRequestRunsWithoutException() {

        CreateOpenFoodFactsAIBuildRequest.main(
            arrayOf("5")
        )
    }
}