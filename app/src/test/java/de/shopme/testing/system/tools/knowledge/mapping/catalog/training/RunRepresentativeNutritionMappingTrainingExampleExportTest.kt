
package de.shopme.testing.system.tools.knowledge.mapping.catalog.training

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.training.RepresentativeNutritionMappingTrainingExampleExporter
import de.shopme.tools.knowledge.report.RejectedLowConfidenceNutritionMappingValidator
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunRepresentativeNutritionMappingTrainingExampleExportTest {

    @Test
    fun exportRepresentativeNutritionMappingTrainingExamples() {

        val projectRoot =
            File("..")

        val reportDirectory =
            File(
                projectRoot,
                "data/generated/knowledge/reports"
            )

        val candidateQualityFile =
            File(
                reportDirectory,
                "nutrition.rejected-candidate-quality.json"
            )

        val diagnosticsFile =
            File(
                reportDirectory,
                "nutrition.match-diagnostics.json"
            )

        val validationFile =
            File(
                reportDirectory,
                "nutrition.low-confidence-validation.json"
            )

        val outputFile =
            File(
                projectRoot,
                "data/generated/knowledge/" +
                        "training/" +
                        "nutrition.representative-" +
                        "mapping-training-examples.json"
            )

        require(candidateQualityFile.isFile) {
            "Rejected nutrition candidate quality file " +
                    "does not exist: " +
                    candidateQualityFile.absolutePath
        }

        require(diagnosticsFile.isFile) {
            "Nutrition match diagnostics file does not exist: " +
                    diagnosticsFile.absolutePath
        }

        val validationResult =
            RejectedLowConfidenceNutritionMappingValidator()
                .run(
                    candidateQualityFile =
                        candidateQualityFile,
                    diagnosticsFile =
                        diagnosticsFile,
                    outputFile =
                        validationFile
                )

        assertTrue(
            validationFile.isFile,
            "Representative nutrition validation file " +
                    "was not created: " +
                    validationFile.absolutePath
        )

        assertTrue(
            validationResult.report.summary
                .rejectedLowConfidenceCount > 0,
            "Representative nutrition validation must " +
                    "contain rejected low-confidence entries."
        )

        assertEquals(
            expected =
                validationResult.report.summary
                    .rejectedLowConfidenceCount,
            actual =
                validationResult.report.entries.size
        )

        val exportResult =
            RepresentativeNutritionMappingTrainingExampleExporter()
                .run(
                    validationFile =
                        validationFile,
                    outputFile =
                        outputFile
                )

        assertTrue(
            outputFile.isFile,
            "Representative nutrition mapping training " +
                    "dataset was not created: " +
                    outputFile.absolutePath
        )

        assertEquals(
            expected =
                validationResult.report.entries.size,
            actual =
                exportResult.dataset.summary
                    .sourceEntryCount
        )

        assertTrue(
            exportResult.dataset.summary
                .exportedExampleCount > 0,
            "Representative nutrition training export " +
                    "must contain accepted examples."
        )

        assertEquals(
            expected =
                exportResult.dataset.summary
                    .exportedExampleCount,
            actual =
                exportResult.dataset.examples.size
        )

        assertEquals(
            expected =
                exportResult.dataset.summary
                    .exportedExampleCount,
            actual =
                exportResult.dataset.summary
                    .identicalCount +
                        exportResult.dataset.summary
                            .representativeCount
        )

        assertTrue(
            exportResult.dataset.examples.all {
                it.accepted
            }
        )

        assertTrue(
            exportResult.dataset.examples.all {
                it.candidateRank > 0
            }
        )

        assertTrue(
            exportResult.dataset.examples.all {
                it.confidence in 0.0..1.0
            }
        )

        val persistedRoot =
            JsonParser.parseString(
                outputFile.readText()
            )
                .asJsonObject

        assertEquals(
            expected = 1,
            actual =
                persistedRoot["version"]
                    .asInt
        )

        assertEquals(
            expected =
                "REPRESENTATIVE_NUTRITION_MAPPING",
            actual =
                persistedRoot["datasetType"]
                    .asString
        )

        assertEquals(
            expected =
                exportResult.dataset.summary
                    .exportedExampleCount,
            actual =
                persistedRoot["examples"]
                    .asJsonArray
                    .size()
        )

        println()
        println(
            "Representative validation entries=" +
                    validationResult.report.summary
                        .rejectedLowConfidenceCount
        )
        println(
            "Identical mappings=" +
                    validationResult.report.summary
                        .identicalCount
        )
        println(
            "Representative mappings=" +
                    validationResult.report.summary
                        .representativeCount
        )
        println(
            "Incompatible mappings=" +
                    validationResult.report.summary
                        .incompatibleCount
        )
        println(
            "Exported training examples=" +
                    exportResult.dataset.summary
                        .exportedExampleCount
        )
        println(
            "Validation artifact=" +
                    validationFile.path
        )
        println(
            "Training dataset=" +
                    outputFile.path
        )
    }
}