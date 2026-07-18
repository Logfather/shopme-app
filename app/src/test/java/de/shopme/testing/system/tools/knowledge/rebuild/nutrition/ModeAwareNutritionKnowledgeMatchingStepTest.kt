package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeMatchingStep
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMatchingResult
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.ModeAwareNutritionKnowledgeMatchingStep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModeAwareNutritionKnowledgeMatchingStepTest {

    @Test
    fun routeOfflineModeToOfflineStep() {

        var offlineCalled =
            false

        var productiveCalled =
            false

        val expected =
            result(
                local =
                    3,
                chatGpt =
                    0,
                fallbackRequired =
                    7
            )

        val adapter =
            ModeAwareNutritionKnowledgeMatchingStep(
                offline =
                    object :
                        NutritionKnowledgeMatchingStep {

                        override fun run(
                            mode:
                            NutritionKnowledgeRebuildMode
                        ): NutritionKnowledgeRebuildMatchingResult {

                            offlineCalled =
                                true

                            assertEquals(
                                expected =
                                    NutritionKnowledgeRebuildMode.OFFLINE,
                                actual =
                                    mode
                            )

                            return expected
                        }
                    },
                productive =
                    object :
                        NutritionKnowledgeMatchingStep {

                        override fun run(
                            mode:
                            NutritionKnowledgeRebuildMode
                        ): NutritionKnowledgeRebuildMatchingResult {

                            productiveCalled =
                                true

                            return result(
                                local = 0,
                                chatGpt = 10,
                                fallbackRequired = 0
                            )
                        }
                    }
            )

        val actual =
            adapter.run(
                mode =
                    NutritionKnowledgeRebuildMode.OFFLINE
            )

        assertTrue(
            offlineCalled
        )

        assertFalse(
            productiveCalled
        )

        assertEquals(
            expected =
                expected,
            actual =
                actual
        )
    }

    @Test
    fun routeProductiveModeToProductiveStep() {

        var offlineCalled =
            false

        var productiveCalled =
            false

        val expected =
            result(
                local =
                    4,
                chatGpt =
                    6,
                fallbackRequired =
                    0
            )

        val adapter =
            ModeAwareNutritionKnowledgeMatchingStep(
                offline =
                    object :
                        NutritionKnowledgeMatchingStep {

                        override fun run(
                            mode:
                            NutritionKnowledgeRebuildMode
                        ): NutritionKnowledgeRebuildMatchingResult {

                            offlineCalled =
                                true

                            return result(
                                local = 10,
                                chatGpt = 0,
                                fallbackRequired = 0
                            )
                        }
                    },
                productive =
                    object :
                        NutritionKnowledgeMatchingStep {

                        override fun run(
                            mode:
                            NutritionKnowledgeRebuildMode
                        ): NutritionKnowledgeRebuildMatchingResult {

                            productiveCalled =
                                true

                            assertEquals(
                                expected =
                                    NutritionKnowledgeRebuildMode.PRODUCTIVE,
                                actual =
                                    mode
                            )

                            return expected
                        }
                    }
            )

        val actual =
            adapter.run(
                mode =
                    NutritionKnowledgeRebuildMode.PRODUCTIVE
            )

        assertFalse(
            offlineCalled
        )

        assertTrue(
            productiveCalled
        )

        assertEquals(
            expected =
                expected,
            actual =
                actual
        )
    }

    @Test
    fun rejectProductiveModeWhenProductiveStepIsMissing() {

        val adapter =
            ModeAwareNutritionKnowledgeMatchingStep(
                offline =
                    object :
                        NutritionKnowledgeMatchingStep {

                        override fun run(
                            mode:
                            NutritionKnowledgeRebuildMode
                        ): NutritionKnowledgeRebuildMatchingResult {

                            return result(
                                local = 1,
                                chatGpt = 0,
                                fallbackRequired = 0
                            )
                        }
                    },
                productive =
                    null
            )

        val exception =
            assertFailsWith<IllegalArgumentException> {

                adapter.run(
                    mode =
                        NutritionKnowledgeRebuildMode.PRODUCTIVE
                )
            }

        assertTrue(
            exception.message
                .orEmpty()
                .contains(
                    "Productive nutrition matching is not configured"
                )
        )
    }

    private fun result(
        local: Int,
        chatGpt: Int,
        fallbackRequired: Int
    ): NutritionKnowledgeRebuildMatchingResult {

        val processed =
            local +
                    chatGpt +
                    fallbackRequired

        return NutritionKnowledgeRebuildMatchingResult(
            requestCount =
                processed,
            previouslyCompletedCount =
                0,
            processedCount =
                processed,
            localModelDecisionCount =
                local,
            chatGptDecisionCount =
                chatGpt,
            gptFallbackRequiredCount =
                fallbackRequired,
            matchCount =
                local + chatGpt,
            noMatchCount =
                0,
            errorCount =
                0
        )
    }
}