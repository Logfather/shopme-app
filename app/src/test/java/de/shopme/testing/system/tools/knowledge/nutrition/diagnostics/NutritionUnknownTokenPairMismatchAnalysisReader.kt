package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

class NutritionUnknownTokenPairMismatchAnalysisReader(
    private val gson: Gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .create(),
) {

    fun read(
        inputFile: File,
    ): NutritionUnknownTokenPairMismatchAnalysis {

        require(inputFile.isFile) {
            "Nutrition UNKNOWN token-pair mismatch report " +
                    "does not exist: ${inputFile.path}"
        }

        val analysis =
            gson.fromJson(
                inputFile.readText(),
                NutritionUnknownTokenPairMismatchAnalysis::class.java,
            )

        require(analysis != null) {
            "Failed to read Nutrition UNKNOWN token-pair mismatch report."
        }

        require(analysis.version >= 2) {
            "Expected Nutrition UNKNOWN token-pair mismatch " +
                    "report version 2 or newer: version=${analysis.version}"
        }

        require(
            analysis.entries.size ==
                    analysis.analyzedRelationshipCount,
        ) {
            "Nutrition UNKNOWN token-pair mismatch report has " +
                    "an inconsistent entry count: entries=" +
                    "${analysis.entries.size}, analyzed=" +
                    analysis.analyzedRelationshipCount
        }

        require(
            analysis.entries.sumOf { entry ->
                entry.observations.size
            } ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Nutrition UNKNOWN token-pair mismatch report has " +
                    "an inconsistent observation count."
        }

        return analysis
    }
}