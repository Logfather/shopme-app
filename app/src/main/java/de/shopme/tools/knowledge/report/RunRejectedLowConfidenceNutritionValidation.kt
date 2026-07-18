package de.shopme.tools.knowledge.report

import java.io.File

object RunRejectedLowConfidenceNutritionValidation {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunRejectedLowConfidenceNutritionValidation " +
                    "does not accept arguments."
        }

        val projectRoot =
            File("..")

        RejectedLowConfidenceNutritionMappingValidator()
            .run(
                candidateQualityFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.rejected-" +
                                "candidate-quality.json"
                    ),
                diagnosticsFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.match-diagnostics.json"
                    ),
                outputFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.low-confidence-validation.json"
                    )
            )
    }
}