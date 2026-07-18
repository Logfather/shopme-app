package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionScoreClusterDiagnosticReportWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create(),
) {

    fun write(
        report: NutritionScoreClusterDiagnosticReport,
        outputFile: File,
    ) {
        val outputDirectory =
            requireNotNull(
                outputFile.parentFile,
            ) {
                "Output file has no parent directory: " +
                        outputFile.absolutePath
            }

        if (!outputDirectory.exists()) {
            check(outputDirectory.mkdirs()) {
                "Could not create output directory: " +
                        outputDirectory.absolutePath
            }
        }

        require(outputDirectory.isDirectory) {
            "Output directory is not a directory: " +
                    outputDirectory.absolutePath
        }

        val temporaryFile =
            File(
                outputDirectory,
                "${outputFile.name}.tmp",
            )

        try {
            temporaryFile.writeText(
                text =
                    gson.toJson(report) + "\n",
                charset =
                    Charsets.UTF_8,
            )

            moveIntoPlace(
                sourceFile = temporaryFile,
                targetFile = outputFile,
            )
        } finally {
            if (temporaryFile.exists()) {
                check(temporaryFile.delete()) {
                    "Could not delete temporary diagnostic report: " +
                            temporaryFile.absolutePath
                }
            }
        }

        check(outputFile.isFile) {
            "Nutrition score-cluster diagnostic report " +
                    "was not written: ${outputFile.absolutePath}"
        }

        check(outputFile.length() > 0L) {
            "Nutrition score-cluster diagnostic report " +
                    "is empty: ${outputFile.absolutePath}"
        }
    }

    private fun moveIntoPlace(
        sourceFile: File,
        targetFile: File,
    ) {
        try {
            Files.move(
                sourceFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        } catch (
            exception: AtomicMoveNotSupportedException,
        ) {
            Files.move(
                sourceFile.toPath(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
            )
        }
    }
}