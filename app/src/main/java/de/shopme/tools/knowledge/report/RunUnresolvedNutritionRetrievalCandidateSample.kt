package de.shopme.tools.knowledge.report

import java.io.File

object RunUnresolvedNutritionRetrievalCandidateSample {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunUnresolvedNutritionRetrievalCandidateSample " +
                    "does not accept arguments."
        }

        val projectRoot =
            File("..")

        UnresolvedNutritionRetrievalCandidateSampler()
            .run(
                unresolvedFailureFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.unresolved-retrieval-failures.json"
                    ),
                outputFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.unresolved-retrieval-candidate-sample.json"
                    )
            )
    }
}