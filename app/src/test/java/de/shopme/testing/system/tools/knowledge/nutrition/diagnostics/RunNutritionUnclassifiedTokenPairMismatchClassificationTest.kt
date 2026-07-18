package de.shopme.testing.system.tools.knowledge.nutrition.diagnostic

import com.google.gson.GsonBuilder
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnclassifiedPartialTokenPairAnalysis
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnclassifiedTokenPairMismatchClassificationWriter
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionUnclassifiedTokenPairMismatchClassifier
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionUnclassifiedTokenPairMismatchClassificationTest {

    @Test
    fun runNutritionUnclassifiedTokenPairMismatchClassification() {
        val sourceFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.unclassified-partial-token-pairs.json",
            )

        val outputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.unclassified-token-pair-mismatches.json",
            )

        require(sourceFile.isFile) {
            "Nutrition unclassified token-pair analysis does not exist: " +
                    sourceFile.absolutePath
        }

        val gson =
            GsonBuilder()
                .serializeNulls()
                .disableHtmlEscaping()
                .create()

        val source =
            sourceFile
                .reader(Charsets.UTF_8)
                .use { reader ->
                    gson.fromJson(
                        reader,
                        NutritionUnclassifiedPartialTokenPairAnalysis::class.java,
                    )
                }

        val classification =
            NutritionUnclassifiedTokenPairMismatchClassifier()
                .classify(
                    source = source,
                )

        NutritionUnclassifiedTokenPairMismatchClassificationWriter()
            .write(
                classification = classification,
                outputFile = outputFile,
            )

        assertEquals(
            expected =
                classification.sourceRelationshipCount,
            actual =
                classification.classifiedRelationshipCount,
        )

        assertEquals(
            expected =
                classification.sourceTokenPairObservationCount,
            actual =
                classification.classifiedTokenPairObservationCount,
        )

        assertEquals(
            expected =
                classification.classifiedTokenPairObservationCount,
            actual =
                classification.countsByMismatchType
                    .values
                    .sum(),
        )

        assertEquals(
            expected =
                classification.classifiedRelationshipCount,
            actual =
                classification
                    .countsByPrimaryRelationshipMismatchType
                    .values
                    .sum(),
        )

        assertTrue(
            classification.entries.isNotEmpty(),
            "Expected classified mismatch entries.",
        )

        assertTrue(
            outputFile.isFile,
            "Expected mismatch-classification output file.",
        )

        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "NUTRITION UNCLASSIFIED TOKEN-PAIR MISMATCHES",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Source relationships      : " +
                    classification.sourceRelationshipCount,
        )
        println(
            "Classified relationships  : " +
                    classification.classifiedRelationshipCount,
        )
        println(
            "Source observations       : " +
                    classification.sourceTokenPairObservationCount,
        )
        println(
            "Classified observations   : " +
                    classification.classifiedTokenPairObservationCount,
        )
        println(
            "Observation mismatch types: " +
                    classification.countsByMismatchType,
        )
        println(
            "Primary relationship types: " +
                    classification.countsByPrimaryRelationshipMismatchType,
        )
        println(
            "Detected relationship types: " +
                    classification.countsByRelationshipMismatchType,
        )
        println(
            "Single-token pair counts  : " +
                    classification.countsBySingleTokenPair,
        )

        classification.topPairsByMismatchType
            .forEach { (mismatchType, pairs) ->
                println(
                    "Top $mismatchType: " +
                            pairs.take(10),
                )
            }

        println(
            "Output                    : " +
                    outputFile.path,
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println()
    }
}