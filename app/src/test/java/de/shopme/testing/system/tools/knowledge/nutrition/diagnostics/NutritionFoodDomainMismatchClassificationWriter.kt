package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class NutritionFoodDomainMismatchClassificationWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create(),
) {

    fun write(
        classification:
        NutritionFoodDomainMismatchClassification,
        outputFile: File,
    ) {
        validate(
            classification = classification,
        )

        outputFile.parentFile?.mkdirs()

        outputFile.writeText(
            gson.toJson(
                classification,
            ),
        )
    }

    private fun validate(
        classification:
        NutritionFoodDomainMismatchClassification,
    ) {
        require(classification.version > 0) {
            "Food-Domain mismatch classification version must " +
                    "be greater than zero."
        }

        require(
            classification.sourceRelationshipCount ==
                    classification.classifiedRelationshipCount,
        ) {
            "Food-Domain mismatch classification does not cover " +
                    "all relationships: source=" +
                    "${classification.sourceRelationshipCount}, " +
                    "classified=" +
                    classification.classifiedRelationshipCount
        }

        require(
            classification.sourceObservationCount ==
                    classification.classifiedObservationCount,
        ) {
            "Food-Domain mismatch classification does not cover " +
                    "all observations: source=" +
                    "${classification.sourceObservationCount}, " +
                    "classified=" +
                    classification.classifiedObservationCount
        }

        require(
            classification.entries.size ==
                    classification.classifiedRelationshipCount,
        ) {
            "Food-Domain mismatch entry count does not match " +
                    "classified relationships: entries=" +
                    "${classification.entries.size}, " +
                    "classified=" +
                    classification.classifiedRelationshipCount
        }

        require(
            classification.entries.sumOf { entry ->
                entry.observations.size
            } ==
                    classification.classifiedObservationCount,
        ) {
            "Food-Domain mismatch entries do not contain all " +
                    "classified observations."
        }

        require(
            classification
                .countsByPrimaryMismatchType
                .values
                .sum() ==
                    classification.classifiedRelationshipCount,
        ) {
            "Primary Food-Domain mismatch counts do not cover " +
                    "all classified relationships."
        }

        require(
            classification
                .countsByObservationMismatchType
                .values
                .sum() ==
                    classification.classifiedObservationCount,
        ) {
            "Observation Food-Domain mismatch counts do not cover " +
                    "all classified observations."
        }

        require(
            classification
                .countsByDomainClassPair
                .values
                .sum() ==
                    classification.classifiedObservationCount,
        ) {
            "Food-Domain class-pair counts do not cover all " +
                    "classified observations."
        }
    }
}