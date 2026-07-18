package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionNoRequestGapDiagnosticReportWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun write(
        report: NutritionNoRequestGapDiagnosticReport,
        outputFile: File
    ) {
        val parentDirectory =
            outputFile.parentFile

        if (parentDirectory != null) {
            require(
                parentDirectory.isDirectory ||
                        parentDirectory.mkdirs()
            ) {
                "Could not create report directory: " +
                        parentDirectory.absolutePath
            }
        }

        val temporaryFile =
            File(
                parentDirectory,
                outputFile.name + ".tmp"
            )

        temporaryFile.writeText(
            gson.toJson(
                report
            ) + "\n"
        )

        try {
            Files.move(
                temporaryFile.toPath(),
                outputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )
        } catch (
            unsupported:
            java.nio.file.AtomicMoveNotSupportedException
        ) {
            Files.move(
                temporaryFile.toPath(),
                outputFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }
}