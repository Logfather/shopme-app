package de.shopme.testing.system.tools.knowledge.nutrition.runner

import com.google.gson.JsonParser
import de.shopme.testing.system.tools.knowledge.nutrition.training.NutritionMatcherTrainingDatasetDomainFeatureEnricher
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionMatcherTrainingDatasetDomainFeatureExportTest {

    @Test
    fun exportDomainMismatchFeaturesIntoNutritionTrainingDataset() {
        val datasetFile =
            File(
                "../data/generated/knowledge/training/" +
                        "nutrition.matcher-training-dataset.json",
            )

        val mismatchReportFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.food-domain-mismatches.json",
            )

        val result =
            NutritionMatcherTrainingDatasetDomainFeatureEnricher()
                .enrich(
                    datasetFile = datasetFile,
                    mismatchReportFile =
                        mismatchReportFile,
                    outputFile = datasetFile,
                )

        assertTrue(
            datasetFile.isFile,
            "Expected enriched Nutrition matcher training dataset.",
        )

        assertEquals(
            result.datasetExampleCount,
            result.enrichedExampleCount,
        )

        val root =
            JsonParser.parseString(
                datasetFile.readText(),
            ).asJsonObject

        assertEquals(
            1,
            root.get("domainMismatchFeatureVersion").asInt,
        )

        val examples =
            root.getAsJsonArray("examples")

        assertEquals(
            result.enrichedExampleCount,
            examples.size(),
        )

        examples.forEachIndexed { index, element ->
            assertTrue(
                element.asJsonObject.has(
                    "domainMismatchFeatures",
                ),
                "Missing Domain-Mismatch features at " +
                        "example index $index.",
            )
        }

        println()
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "NUTRITION TRAINING DOMAIN-MISMATCH FEATURES",
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
        println(
            "Dataset examples          : " +
                    result.datasetExampleCount,
        )
        println(
            "Enriched examples         : " +
                    result.enrichedExampleCount,
        )
        println(
            "Matched relationships     : " +
                    result.matchedRelationshipCount,
        )
        println(
            "Unmatched relationships   : " +
                    result.unmatchedRelationshipCount,
        )
        println(
            "Mismatch report entries   : " +
                    result.mismatchIndexEntryCount,
        )
        println(
            "Output                    : " +
                    result.outputFile.path,
        )
        println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
        )
    }
}