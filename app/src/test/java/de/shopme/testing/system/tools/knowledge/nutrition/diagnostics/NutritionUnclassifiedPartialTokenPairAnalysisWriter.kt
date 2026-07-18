package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionUnclassifiedPartialTokenPairAnalysisWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create(),
) {

    fun write(
        analysis:
        NutritionUnclassifiedPartialTokenPairAnalysis,
        outputFile: File,
    ) {
        require(analysis.version > 0) {
            "Token-pair analysis has an invalid version: " +
                    analysis.version
        }

        require(
            analysis.sourceUnclassifiedPartialCount ==
                    analysis.analyzedRelationshipCount,
        ) {
            "Token-pair analysis is incomplete: " +
                    "source=${analysis.sourceUnclassifiedPartialCount}, " +
                    "analyzed=${analysis.analyzedRelationshipCount}"
        }

        require(
            analysis.singleTokenPairRelationshipCount +
                    analysis.multiTokenRelationshipCount ==
                    analysis.analyzedRelationshipCount,
        ) {
            "Relationship shape counts do not cover all analyzed entries."
        }

        require(
            analysis.tokenPairObservationCount ==
                    analysis.countsByTokenPair.values.sum(),
        ) {
            "Raw token-pair counts do not cover all observations."
        }

        require(
            analysis.tokenPairObservationCount ==
                    analysis.countsByNormalizedTokenPair.values.sum(),
        ) {
            "Normalized token-pair counts do not cover all observations."
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
                    "Could not delete temporary token-pair analysis: " +
                            temporaryFile.absolutePath
                }
            }
        }

        check(outputFile.isFile) {
            "Token-pair analysis was not written: " +
                    outputFile.absolutePath
        }

        check(outputFile.length() > 0L) {
            "Token-pair analysis output is empty: " +
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