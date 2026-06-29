package de.shopme.testing.system.tools.knowledge.update

import de.shopme.tools.knowledge.update.steps.PublishRuntimeKnowledgeStep
import org.junit.Test
import java.io.File

class PublishRuntimeKnowledgeStepTest {

    @Test
    fun publishCopiesCarbonKnowledgeToRuntimeAssets() {

        val generated =

            File(
                "build/generated/carbon_footprint.json"
            )

        generated.parentFile?.mkdirs()

        generated.writeText(
            """
            {
              "entries": {
                "apple": {
                  "kilogramsPerKilogram": 0.5
                }
              }
            }
            """.trimIndent()
        )

        val step =
            PublishRuntimeKnowledgeStep()

        step.publish()

        val runtime =

            File(
                "app/src/main/assets/knowledge/runtime/carbon_footprint.json"
            )

        assert(
            runtime.exists()
        )

        assert(
            runtime.length() > 0
        )
    }
}