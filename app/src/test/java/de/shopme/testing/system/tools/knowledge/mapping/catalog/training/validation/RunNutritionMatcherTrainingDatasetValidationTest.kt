package de.shopme.testing.system.tools.knowledge.mapping.catalog.training.validation

import de.shopme.tools.knowledge.mapping.catalog.training.validation.DeterministicNutritionMatcherTrainingDatasetValidator
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionMatcherTrainingDatasetValidationTest {

    @Test
    fun validateGeneratedNutritionMatcherTrainingDataset() {

        val projectRoot =
            File("..")

        val datasetFile =
            File(
                projectRoot,
                "data/generated/knowledge/" +
                        "training/" +
                        "nutrition.matcher-training-dataset.json"
            )

        require(datasetFile.isFile) {
            "Nutrition matcher training dataset does not exist: " +
                    datasetFile.absolutePath
        }

        val result =
            DeterministicNutritionMatcherTrainingDatasetValidator()
                .validateOrThrow(
                    datasetFile =
                        datasetFile
                )

        assertTrue(
            result.valid
        )

        assertEquals(
            expected = 0,
            actual =
                result.issueCount
        )

        assertEquals(
            expected = 4256,
            actual =
                result.exampleCount
        )

        assertEquals(
            expected = 477,
            actual =
                result.positiveCount
        )

        assertEquals(
            expected = 3779,
            actual =
                result.negativeCount
        )

        println()
        println(
            "Validated nutrition matcher examples=" +
                    result.exampleCount
        )
        println(
            "Validated positive examples=" +
                    result.positiveCount
        )
        println(
            "Validated negative examples=" +
                    result.negativeCount
        )
        println(
            "Dataset validation issues=" +
                    result.issueCount
        )
        println(
            "Validated dataset=" +
                    datasetFile.path
        )
    }
}