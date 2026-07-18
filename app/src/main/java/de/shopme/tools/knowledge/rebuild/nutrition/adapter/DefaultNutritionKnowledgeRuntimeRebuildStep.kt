package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRuntimeRebuildStep
import java.io.File

class DefaultNutritionKnowledgeRuntimeRebuildStep(
    private val runtimeNutritionFile: File,
    private val rebuildRuntime: () -> Unit
) : NutritionKnowledgeRuntimeRebuildStep {

    override fun run() {

        val previousLastModified =
            runtimeNutritionFile
                .takeIf {
                    it.isFile
                }
                ?.lastModified()

        rebuildRuntime()

        require(runtimeNutritionFile.isFile) {
            "Nutrition runtime artifact was not created: " +
                    runtimeNutritionFile.absolutePath
        }

        previousLastModified
            ?.let {
                require(
                    runtimeNutritionFile.lastModified() >= it
                ) {
                    "Nutrition runtime artifact became older " +
                            "during rebuild."
                }
            }
    }
}