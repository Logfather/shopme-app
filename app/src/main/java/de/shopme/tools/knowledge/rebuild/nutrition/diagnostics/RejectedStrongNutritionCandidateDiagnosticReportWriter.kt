package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class RejectedStrongNutritionCandidateDiagnosticReportWriter(
    private val outputFile: File
) {

    fun write(
        report: RejectedStrongNutritionCandidateDiagnosticReport
    ) {
        val directory =
            requireNotNull(
                outputFile.parentFile
            ) {
                "Output file has no parent directory."
            }

        if (!directory.exists()) {
            check(directory.mkdirs()) {
                "Could not create rejected strong nutrition " +
                        "candidate report directory: " +
                        directory.absolutePath
            }
        }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        val temporaryFile =
            File(
                directory,
                outputFile.name +
                        ".tmp"
            )

        temporaryFile.writeText(
            gson.toJson(
                report
            ) +
                    "\n"
        )

        try {
            Files.move(
                temporaryFile.toPath(),
                outputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )

        } catch (
            exception: AtomicMoveNotSupportedException
        ) {
            Files.move(
                temporaryFile.toPath(),
                outputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }

        check(outputFile.isFile) {
            "Rejected strong nutrition candidate report was not " +
                    "written: " +
                    outputFile.absolutePath
        }
    }
}