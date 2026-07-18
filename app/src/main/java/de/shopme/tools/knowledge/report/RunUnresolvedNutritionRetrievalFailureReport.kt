package de.shopme.tools.knowledge.report

import java.io.File

object RunUnresolvedNutritionRetrievalFailureReport {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunUnresolvedNutritionRetrievalFailureReport " +
                    "does not accept arguments."
        }

        val projectRoot =
            File("..")

        UnresolvedNutritionRetrievalFailureReporter()
            .run(
                retrievalFailureFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.retrieval-failures.json"
                    ),
                outputFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.unresolved-retrieval-failures.json"
                    )
            )
    }
}