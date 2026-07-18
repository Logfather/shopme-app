package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionMatchPersistenceDiagnosticReportWriter(
    private val outputFile: File
) {

    fun write(
        report: NutritionMatchPersistenceDiagnosticReport
    ) {
        outputFile.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create nutrition match persistence " +
                                "diagnostic directory: " +
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
            gson.toJson(
                report
            ) +
                    "\n"
        )

        Files.move(
            temporaryFile.toPath(),
            outputFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
        )

        check(outputFile.isFile) {
            "Nutrition match persistence diagnostic report was not " +
                    "written: " +
                    outputFile.absolutePath
        }
    }
}