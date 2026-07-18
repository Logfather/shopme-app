package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeMatchingStep
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMatchingResult
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode

class ModeAwareNutritionKnowledgeMatchingStep(
    private val offline:
    NutritionKnowledgeMatchingStep,
    private val productive:
    NutritionKnowledgeMatchingStep?
) : NutritionKnowledgeMatchingStep {

    override fun run(
        mode: NutritionKnowledgeRebuildMode
    ): NutritionKnowledgeRebuildMatchingResult {

        return when (mode) {

            NutritionKnowledgeRebuildMode.OFFLINE ->
                offline.run(
                    mode =
                        NutritionKnowledgeRebuildMode.OFFLINE
                )

            NutritionKnowledgeRebuildMode.PRODUCTIVE ->
                requireNotNull(
                    productive
                ) {
                    "Productive nutrition matching is not configured."
                }
                    .run(
                        mode =
                            NutritionKnowledgeRebuildMode.PRODUCTIVE
                    )
        }
    }
}