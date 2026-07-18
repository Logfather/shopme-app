package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.knowledge.report.RunUnresolvedNutritionRetrievalFailureReport
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class RunUnresolvedNutritionRetrievalFailureReportTest {

    @Test
    fun runUnresolvedNutritionRetrievalFailureReport() {

        RunUnresolvedNutritionRetrievalFailureReport.main(
            emptyArray()
        )

        val outputFile =
            File(
                "../data/generated/knowledge/" +
                        "reports/" +
                        "nutrition.unresolved-retrieval-failures.json"
            )

        assertTrue(
            actual = outputFile.isFile,
            message =
                "Expected unresolved retrieval report: " +
                        outputFile.absolutePath
        )

        assertTrue(
            actual = outputFile.length() > 0L,
            message =
                "Unresolved retrieval report must not be empty: " +
                        outputFile.absolutePath
        )
    }
}