package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.knowledge.report.RunUnresolvedNutritionRetrievalCandidateSample
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class RunUnresolvedNutritionRetrievalCandidateSampleTest {

    @Test
    fun runUnresolvedNutritionRetrievalCandidateSample() {

        RunUnresolvedNutritionRetrievalCandidateSample.main(
            emptyArray()
        )

        val outputFile =
            File(
                "../data/generated/knowledge/" +
                        "reports/" +
                        "nutrition.unresolved-retrieval-candidate-sample.json"
            )

        assertTrue(
            actual = outputFile.isFile,
            message =
                "Expected unresolved candidate sample: " +
                        outputFile.absolutePath
        )

        assertTrue(
            actual = outputFile.length() > 0L,
            message =
                "Unresolved candidate sample must not be empty: " +
                        outputFile.absolutePath
        )
    }
}