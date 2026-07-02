package de.shopme.testing.system.tools.knowledge.update

import de.shopme.tools.knowledge.update.AdminKnowledgeUpdateService
import de.shopme.tools.knowledge.update.steps.BuildCarbonKnowledgeStep
import de.shopme.tools.knowledge.update.steps.PublishRuntimeKnowledgeStep
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class AdminKnowledgeUpdateServiceTest {

    @Test
    fun runsCarbonKnowledgeUpdateStep() {

        val service =

            AdminKnowledgeUpdateService(

                steps =
                    listOf(

                        BuildCarbonKnowledgeStep(),

                        PublishRuntimeKnowledgeStep()

                    )
            )

        val result =
            service.runWeeklyUpdate()

        assertTrue(
            result.success
        )

        val output =
            File(
                "data/generated/carbon_footprint.json"
            )

        assertTrue(
            output.exists()
        )

        assertTrue(
            output.length() > 0
        )

        val runtimeFile =

            File(
                "src/main/assets/knowledge/runtime/carbon_footprint.json"
            )

        assert(
            runtimeFile.exists()
        )

        assert(
            runtimeFile.length() > 0
        )
    }
}