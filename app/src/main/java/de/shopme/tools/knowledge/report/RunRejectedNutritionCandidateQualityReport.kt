package de.shopme.tools.knowledge.report

import java.io.File

object RunRejectedNutritionCandidateQualityReport {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunRejectedNutritionCandidateQualityReport " +
                    "does not accept arguments."
        }

        val projectRoot =
            File("..")

        RejectedNutritionCandidateQualityReporter()
            .run(
                requestFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "match-requests/" +
                                "nutrition.match-requests.json"
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
                                "nutrition.rejected-candidate-quality.json"
                    )
            )
    }
}