package de.shopme.testing.system.tools.knowledge.test.enricher.domains

import de.shopme.tools.knowledge.update.steps.BuildCarbonKnowledgeStep
import org.junit.Test
import java.io.File

class CarbonKnowledgeBuildTest {

    @Test
    fun buildCompilerCreatesCarbonKnowledge() {

        BuildCarbonKnowledgeStep()

            .execute()

        val output =

            File(
                "data/generated/carbon_footprint.json"
            )

        assert(
            output.exists()
        )

        assert(
            output.length() > 0
        )
    }
}