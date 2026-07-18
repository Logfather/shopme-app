package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionPartialCandidateRelationshipAnalysisWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create(),
) {

    fun write(
        analysis:
        NutritionPartialCandidateRelationshipAnalysis,
        outputFile: File,
    ) {
        require(analysis.version > 0) {
            "Partial candidate relationship analysis has an invalid version: " +
                    analysis.version
        }

        require(
            analysis.partialCandidateCount ==
                    analysis.classifiedCandidateCount,
        ) {
            "Not every partial candidate was classified: " +
                    "partial=${analysis.partialCandidateCount}, " +
                    "classified=${analysis.classifiedCandidateCount}"
        }

        require(
            analysis.countsByPrimaryRelationshipType
                .values
                .sum() ==
                    analysis.classifiedCandidateCount,
        ) {
            "Primary relationship counts do not cover all classified candidates."
        }

        val outputDirectory =
            requireNotNull(outputFile.parentFile) {
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
                    gson.toJson(analysis) + "\n",
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
                    "Could not delete temporary analysis file: " +
                            temporaryFile.absolutePath
                }
            }
        }

        check(outputFile.isFile) {
            "Partial candidate relationship report was not written: " +
                    outputFile.absolutePath
        }

        check(outputFile.length() > 0L) {
            "Partial candidate relationship report is empty: " +
                    outputFile.absolutePath
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