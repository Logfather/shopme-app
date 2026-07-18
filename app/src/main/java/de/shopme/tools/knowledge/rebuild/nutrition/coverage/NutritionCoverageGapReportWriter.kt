package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import com.google.gson.GsonBuilder
import java.io.File

class NutritionCoverageGapReportWriter(
    private val outputFile: File
) {

    fun write(
        report: NutritionCoverageGapReport
    ) {
        outputFile.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create nutrition coverage gap " +
                                "report directory: " +
                                directory.absolutePath
                    }
                }
            }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        val temporaryFile =
            File(
                outputFile.parentFile,
                outputFile.name +
                        ".tmp"
            )

        temporaryFile.writeText(
            gson.toJson(report) +
                    "\n"
        )

        if (outputFile.exists()) {
            check(outputFile.delete()) {
                "Could not replace existing nutrition coverage gap " +
                        "report: " +
                        outputFile.absolutePath
            }
        }

        if (!temporaryFile.renameTo(outputFile)) {

            outputFile.writeText(
                temporaryFile.readText()
            )

            check(temporaryFile.delete()) {
                "Could not remove temporary nutrition coverage gap " +
                        "report: " +
                        temporaryFile.absolutePath
            }
        }

        check(outputFile.isFile) {
            "Nutrition coverage gap report was not written: " +
                    outputFile.absolutePath
        }
    }
}