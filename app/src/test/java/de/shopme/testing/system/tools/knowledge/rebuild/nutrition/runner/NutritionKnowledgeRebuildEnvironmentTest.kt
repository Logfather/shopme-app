package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.runner

import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NutritionKnowledgeRebuildEnvironmentTest {

    @Test
    fun defaultToOfflineMode() {

        assertEquals(
            expected =
                NutritionKnowledgeRebuildMode.OFFLINE,
            actual =
                NutritionKnowledgeRebuildEnvironment
                    .readMode(
                        environment =
                            emptyMap()
                    )
        )
    }

    @Test
    fun readProductiveMode() {

        assertEquals(
            expected =
                NutritionKnowledgeRebuildMode.PRODUCTIVE,
            actual =
                NutritionKnowledgeRebuildEnvironment
                    .readMode(
                        environment =
                            mapOf(
                                NutritionKnowledgeRebuildEnvironment
                                    .MODE_ENVIRONMENT_VARIABLE to
                                        "productive"
                            )
                    )
        )
    }

    @Test
    fun requireExplicitProductiveOpenAiFlag() {

        assertFailsWith<IllegalArgumentException> {

            NutritionKnowledgeRebuildEnvironment
                .requireProductiveOpenAIEnabled(
                    mode =
                        NutritionKnowledgeRebuildMode.PRODUCTIVE,
                    environment =
                        emptyMap()
                )
        }

        NutritionKnowledgeRebuildEnvironment
            .requireProductiveOpenAIEnabled(
                mode =
                    NutritionKnowledgeRebuildMode.PRODUCTIVE,
                environment =
                    mapOf(
                        NutritionKnowledgeRebuildEnvironment
                            .PRODUCTIVE_OPENAI_ENVIRONMENT_VARIABLE to
                                "true"
                    )
            )
    }
}