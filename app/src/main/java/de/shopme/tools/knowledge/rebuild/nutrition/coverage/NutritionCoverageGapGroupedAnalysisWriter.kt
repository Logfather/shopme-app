package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionCoverageGapGroupedAnalysisWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()
) {

    fun write(
        report: NutritionCoverageGapGroupedAnalysis,
        outputFile: File
    ) {
        outputFile.parentFile
            ?.let { parent ->

                require(
                    parent.exists() ||
                            parent.mkdirs()
                ) {
                    "Could not create output directory: " +
                            parent.absolutePath
                }
            }

        val temporaryFile =
            File(
                outputFile.parentFile,
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