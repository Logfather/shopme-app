package de.shopme.tools.knowledge.mapping.catalog.representative

import java.io.File

object RunPersistRepresentativeNutritionMappings {

    @JvmStatic
    fun main(
        args: Array<String>
    ) {
        require(args.isEmpty()) {
            "RunPersistRepresentativeNutritionMappings " +
                    "does not accept arguments."
        }

        val projectRoot =
            File("..")

        PersistRepresentativeNutritionMappings()
            .run(
                validationFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "reports/" +
                                "nutrition.low-confidence-validation.json"
                    ),
                mappingFile =
                    File(
                        projectRoot,
                        "data/generated/knowledge/" +
                                "mappings/" +
                                "catalog-server.mappings.json"
                    )
            )
    }
}