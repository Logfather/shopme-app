package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class RejectedStrongNutritionCandidateValidationReportWriter(
    private val outputFile: File
) {

    fun write(
        report:
        RejectedStrongNutritionCandidateValidationReport
    ) {
        val parentDirectory =
            requireNotNull(
                outputFile.parentFile
            ) {
                "Validation report output file has no parent directory."
            }

        if (!parentDirectory.exists()) {
            check(parentDirectory.mkdirs()) {
                "Could not create rejected strong nutrition " +
                        "validation report directory: " +
                        parentDirectory.absolutePath
            }
        }

        val temporaryFile =
            File(
                parentDirectory,
                outputFile.name +
                        ".tmp"
            )

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

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
            "Rejected strong nutrition candidate validation report " +
                    "was not written: " +
                    outputFile.absolutePath
        }
    }
}