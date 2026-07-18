package de.shopme.tools.knowledge.rebuild.nutrition.runner

import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode

object NutritionKnowledgeRebuildEnvironment {

    fun readMode(
        environment:
        Map<String, String> =
            System.getenv()
    ): NutritionKnowledgeRebuildMode {

        val value =
            environment[MODE_ENVIRONMENT_VARIABLE]
                ?.trim()
                ?.uppercase()
                ?: DEFAULT_MODE

        return runCatching {
            NutritionKnowledgeRebuildMode.valueOf(
                value
            )
        }
            .getOrElse {
                error(
                    "Invalid $MODE_ENVIRONMENT_VARIABLE='$value'. " +
                            "Supported values: OFFLINE, PRODUCTIVE."
                )
            }
    }

    fun requireProductiveOpenAIEnabled(
        mode: NutritionKnowledgeRebuildMode,
        environment:
        Map<String, String> =
            System.getenv()
    ) {
        if (
            mode !=
            NutritionKnowledgeRebuildMode.PRODUCTIVE
        ) {
            return
        }

        val enabled =
            environment[
                PRODUCTIVE_OPENAI_ENVIRONMENT_VARIABLE
            ]
                ?.trim()
                ?.equals(
                    other = "true",
                    ignoreCase = true
                )
                ?: false

        require(enabled) {
            "Productive nutrition rebuild with ChatGPT is " +
                    "disabled. Set " +
                    "$PRODUCTIVE_OPENAI_ENVIRONMENT_VARIABLE=true."
        }
    }

    const val MODE_ENVIRONMENT_VARIABLE =
        "NUTRITION_KNOWLEDGE_REBUILD_MODE"

    const val PRODUCTIVE_OPENAI_ENVIRONMENT_VARIABLE =
        "RUN_NUTRITION_KNOWLEDGE_REBUILD_OPENAI"

    private const val DEFAULT_MODE =
        "OFFLINE"
}