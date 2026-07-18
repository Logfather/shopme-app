package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionUnknownTokenPairMismatchAnalysisWriter(
    private val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create(),
) {

    fun write(
        analysis: NutritionUnknownTokenPairMismatchAnalysis,
        outputFile: File,
    ) {
        validate(
            analysis = analysis,
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
                    "Could not delete temporary UNKNOWN analysis file: " +
                            temporaryFile.absolutePath
                }
            }
        }

        check(outputFile.isFile) {
            "UNKNOWN token-pair analysis was not written: " +
                    outputFile.absolutePath
        }

        check(outputFile.length() > 0L) {
            "UNKNOWN token-pair analysis output is empty: " +
                    outputFile.absolutePath
        }
    }

    private fun validate(
        analysis: NutritionUnknownTokenPairMismatchAnalysis,
    ) {
        require(analysis.version > 0) {
            "UNKNOWN token-pair analysis has an invalid version: " +
                    analysis.version
        }

        require(
            analysis.sourcePrimaryUnknownRelationshipCount ==
                    analysis.analyzedRelationshipCount,
        ) {
            "UNKNOWN token-pair analysis does not cover all primary " +
                    "UNKNOWN relationships."
        }

        require(
            analysis.singleTokenPairRelationshipCount +
                    analysis.multiTokenRelationshipCount ==
                    analysis.analyzedRelationshipCount,
        ) {
            "Relationship-shape counts do not cover all analyzed relationships."
        }

        require(
            analysis.countsByTokenPair.values.sum() ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Token-pair counts do not cover all analyzed observations."
        }

        require(
            analysis.countsByCatalogTokenKind.values.sum() ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Catalog token-kind counts do not cover all observations."
        }

        require(
            analysis.countsByServerTokenKind.values.sum() ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Server token-kind counts do not cover all observations."
        }

        require(
            analysis.countsByCatalogFoodDomainClass.values.sum() ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Catalog Food-Domain class counts do not cover all observations."
        }

        require(
            analysis.countsByServerFoodDomainClass.values.sum() ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Server Food-Domain class counts do not cover all observations."
        }

        require(
            analysis.countsByFoodDomainClassPair.values.sum() ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Food-Domain class-pair counts do not cover all observations."
        }

        require(
            analysis.countsByPairProfile.values.sum() ==
                    analysis.analyzedTokenPairObservationCount,
        ) {
            "Pair-profile counts do not cover all observations."
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