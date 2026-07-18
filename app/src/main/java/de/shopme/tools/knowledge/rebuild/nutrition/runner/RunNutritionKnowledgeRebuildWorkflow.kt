package de.shopme.tools.knowledge.rebuild.nutrition.runner

import java.io.File
import kotlin.system.exitProcess

object RunNutritionKnowledgeRebuildWorkflow {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunNutritionKnowledgeRebuildWorkflow " +
                    "does not accept arguments. Configure it using " +
                    "environment variables."
        }

        val mode =
            NutritionKnowledgeRebuildEnvironment
                .readMode()

        NutritionKnowledgeRebuildEnvironment
            .requireProductiveOpenAIEnabled(
                mode =
                    mode
            )

        val projectRoot =
            File("..")

        val files =
            NutritionKnowledgeRebuildProjectFiles
                .fromProjectRoot(
                    projectRoot =
                        projectRoot
                )

        val workflow =
            NutritionKnowledgeRebuildWorkflowFactory()
                .create(
                    mode =
                        mode,
                    files =
                        files
                )

        val result =
            workflow.run(
                mode =
                    mode
            )

        if (result.matching.errorCount > 0) {
            exitProcess(2)
        }

        if (
            mode.name == "PRODUCTIVE" &&
            result.matching.gptFallbackRequiredCount > 0
        ) {
            exitProcess(3)
        }
    }
}