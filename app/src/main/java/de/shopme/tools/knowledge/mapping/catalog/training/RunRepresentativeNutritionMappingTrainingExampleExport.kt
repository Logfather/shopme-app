package de.shopme.tools.knowledge.mapping.catalog.training

import java.io.File

object RunRepresentativeNutritionMappingTrainingExampleExport {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunRepresentativeNutritionMappingTrainingExampleExport " +
                    "does not accept arguments."
        }

        val projectRoot =
            File("..")

        RepresentativeNutritionMappingTrainingExampleExporter()
            .run(
                validationFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.low-confidence-validation.json"
                    ),
                outputFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "training/" +
                                "nutrition.representative-" +
                                "mapping-training-examples.json"
                    )
            )
    }
}