package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionUnclassifiedTokenPairMismatchClassificationWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create(),
) {

    fun write(
        classification:
        NutritionUnclassifiedTokenPairMismatchClassification,
        outputFile: File,
    ) {
        validate(
            classification = classification,
        )

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
                    gson.toJson(classification) + "\n",
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
                    "Could not delete temporary mismatch-classification file: " +
                            temporaryFile.absolutePath
                }
            }
        }

        check(outputFile.isFile) {
            "Mismatch-classification report was not written: " +
                    outputFile.absolutePath
        }

        check(outputFile.length() > 0L) {
            "Mismatch-classification report is empty: " +
                    outputFile.absolutePath
        }
    }

    private fun validate(
        classification:
        NutritionUnclassifiedTokenPairMismatchClassification,
    ) {
        require(classification.version > 0) {
            "Mismatch classification has an invalid version: " +
                    classification.version
        }

        require(
            classification.sourceRelationshipCount ==
                    classification.classifiedRelationshipCount,
        ) {
            "Mismatch classification does not cover all relationships."
        }

        require(
            classification.sourceTokenPairObservationCount ==
                    classification.classifiedTokenPairObservationCount,
        ) {
            "Mismatch classification does not cover all observations."
        }

        require(
            classification.countsByMismatchType
                .values
                .sum() ==
                    classification.classifiedTokenPairObservationCount,
        ) {
            "Mismatch counts do not cover all observations."
        }

        require(
            classification.countsByPrimaryRelationshipMismatchType
                .values
                .sum() ==
                    classification.classifiedRelationshipCount,
        ) {
            "Primary relationship mismatch counts do not cover all relationships."
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