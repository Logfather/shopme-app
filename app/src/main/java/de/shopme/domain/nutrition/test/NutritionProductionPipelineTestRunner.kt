package de.shopme.domain.nutrition.test

import de.shopme.data.sync.logging.RuntimeLog
import de.shopme.domain.nutrition.pipeline.ProductionNutritionPipeline

class NutritionProductionPipelineTestRunner(

    private val pipeline: ProductionNutritionPipeline

) {

    suspend fun run() {

        RuntimeLog.runtime(
            "========== NUTRITION PRODUCTION PIPELINE TEST START =========="
        )

        val references = listOf(

            "nutella",
            "vollmilch",
            "butter",
            "banane",
            "coca_cola"

        )

        references.forEach { reference ->

            val detail =
                pipeline.getNutritionDetail(
                    reference
                )

            if (detail == null) {

                RuntimeLog.runtime(

                    "Pipeline Test | " +
                            "reference=$reference | " +
                            "result=NULL"

                )

                return@forEach
            }

            RuntimeLog.runtime(

                "Pipeline Test | " +
                        "reference=$reference | " +
                        "score=${detail.nutriScore} | " +
                        "calories=${detail.values.calories.toInt()} kcal | " +
                        "fat=${detail.values.fat} g | " +
                        "sugar=${detail.values.sugar} g"

            )

        }

        RuntimeLog.runtime(
            "========== NUTRITION PRODUCTION PIPELINE TEST END =========="
        )

    }

}