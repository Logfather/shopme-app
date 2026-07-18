package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.knowledge.report.RunRejectedNutritionRetrievalFailureReport
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class RunRejectedNutritionRetrievalFailureReportTest {

    @Test
    fun runRejectedNutritionRetrievalFailureReport() {

        RunRejectedNutritionRetrievalFailureReport.main(
            emptyArray()
        )

        val outputFile =
            File(
                "../data/generated/knowledge/" +
                        "reports/" +
                        "nutrition.retrieval-failures.json"
            )

        assertTrue(
            actual = outputFile.isFile,
            message =
                "Expected retrieval failure report: " +
                        outputFile.absolutePath
        )

        assertTrue(
            actual = outputFile.length() > 0L,
            message =
                "Retrieval failure report must not be empty: " +
                        outputFile.absolutePath
        )
    }
}