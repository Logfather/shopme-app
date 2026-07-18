package de.shopme.tools.knowledge.report

import java.io.File

object RunRejectedNutritionRetrievalFailureReport {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunRejectedNutritionRetrievalFailureReport " +
                    "does not accept arguments."
        }

        val projectRoot =
            File("..")

        RejectedNutritionRetrievalFailureClassifier()
            .run(
                candidateQualityFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.rejected-candidate-quality.json"
                    ),
                outputFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.retrieval-failures.json"
                    )
            )
    }
}