package de.shopme.testing.system.tools.knowledge.nutrition.diagnostic

import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.DeterministicNutritionFoodDomainMismatchClassifier
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionFoodDomainMismatchClassificationWriter
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionFoodDomainMismatchType
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnknownTokenPairMismatchAnalysisReader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionFoodDomainMismatchClassificationTest {

    @Test
    fun classifyNutritionFoodDomainMismatches() {
        val inputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.unknown-token-pair-mismatches.json",
            )

        val outputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.food-domain-mismatches.json",
            )

        val source =
            NutritionUnknownTokenPairMismatchAnalysisReader()
                .read(
                    inputFile = inputFile,
                )

        val classification =
            DeterministicNutritionFoodDomainMismatchClassifier()
                .classify(
                    source = source,
                )

        NutritionFoodDomainMismatchClassificationWriter()
            .write(
                classification = classification,
                outputFile = outputFile,
            )

        assertEquals(
            expected =
                source.analyzedRelationshipCount,
            actual =
                classification.classifiedRelationshipCount,
        )

        assertEquals(
            expected =
                source.analyzedTokenPairObservationCount,
            actual =
                classification.classifiedObservationCount,
        )

        assertEquals(
            expected =
                classification.classifiedRelationshipCount,
            actual =
                classification
                    .countsByPrimaryMismatchType
                    .values
                    .sum(),
        )

        assertEquals(
            expected =
                classification.classifiedObservationCount,
            actual =
                classification
                    .countsByObservationMismatchType
                    .values
                    .sum(),
        )

        assertEquals(
            expected =
                classification.classifiedObservationCount,
            actual =
                classification
                    .countsByDomainClassPair
                    .values
                    .sum(),
        )

        assertTrue(
            actual =
                outputFile.isFile,
            message =
                "Expected Food-Domain mismatch report: " +
                        outputFile.path,
        )

        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "NUTRITION FOOD-DOMAIN MISMATCH CLASSIFICATION",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Source relationships       : " +
                    classification.sourceRelationshipCount,
        )
        println(
            "Source observations        : " +
                    classification.sourceObservationCount,
        )
        println(
            "Classified relationships   : " +
                    classification.classifiedRelationshipCount,
        )
        println(
            "Classified observations    : " +
                    classification.classifiedObservationCount,
        )
        println(
            "Primary mismatch types     : " +
                    classification.countsByPrimaryMismatchType,
        )
        println(
            "Observation mismatch types : " +
                    classification.countsByObservationMismatchType,
        )

        enumValues<NutritionFoodDomainMismatchType>()
            .forEach { mismatchType ->
                val examples =
                    classification.entries
                        .asSequence()
                        .filter { entry ->
                            entry.primaryMismatchType ==
                                    mismatchType
                        }
                        .take(10)
                        .map { entry ->
                            "${entry.catalogKey} -> " +
                                    "${entry.serverKey} " +
                                    "(rank=${entry.rank})"
                        }
                        .toList()

                println(
                    "Top $mismatchType: $examples",
                )
            }

        println(
            "Output                     : " +
                    outputFile.path,
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
    }
}