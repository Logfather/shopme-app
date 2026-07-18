package de.shopme.tools.knowledge.rebuild.nutrition

import com.google.gson.GsonBuilder
import java.io.File
import java.io.PrintStream

class NutritionKnowledgeRebuildWorkflow(
    private val snapshotReader:
    NutritionKnowledgeSnapshotReader,
    private val requestRebuilder:
    NutritionKnowledgeRequestRebuilder,
    private val matchingStep:
    NutritionKnowledgeMatchingStep,
    private val mappingPersistenceStep:
    NutritionKnowledgeMappingPersistenceStep,
    private val runtimeRebuildStep:
    NutritionKnowledgeRuntimeRebuildStep,
    private val files:
    NutritionKnowledgeRebuildFiles,
    private val resultFile: File,
    private val output: PrintStream = System.out
) {

    fun run(
        mode: NutritionKnowledgeRebuildMode
    ): NutritionKnowledgeRebuildResult {

        val before =
            snapshotReader.read()

        val requestResult =
            requestRebuilder.rebuild()

        val matching =
            matchingStep.run(
                mode = mode
            )

        require(
            matching.requestCount ==
                    requestResult.requestCount
        ) {
            "Matching request count differs from rebuilt " +
                    "request count."
        }

        val persistence =
            mappingPersistenceStep.run()

        runtimeRebuildStep.run()

        val after =
            snapshotReader.read()

        val result =
            NutritionKnowledgeRebuildResult(
                mode =
                    mode,
                before =
                    before,
                matching =
                    matching,
                persistence =
                    persistence,
                after =
                    after,
                delta =
                    NutritionKnowledgeRebuildDelta(
                        mappingCount =
                            after.mappingCount -
                                    before.mappingCount,
                        coveredCatalogItemCount =
                            after.coveredCatalogItemCount -
                                    before.coveredCatalogItemCount,
                        missingCatalogItemCount =
                            after.missingCatalogItemCount -
                                    before.missingCatalogItemCount,
                        coverage =
                            after.coverage -
                                    before.coverage
                    ),
                files =
                    files
            )

        validateResult(
            result = result
        )

        writeResult(
            result = result
        )

        printResult(
            result = result
        )

        return result
    }

    private fun validateResult(
        result: NutritionKnowledgeRebuildResult
    ) {
        require(
            result.after.mappingCount >=
                    result.before.mappingCount
        ) {
            "Nutrition rebuild must not remove mappings."
        }

        require(
            result.after.coveredCatalogItemCount >=
                    result.before.coveredCatalogItemCount
        ) {
            "Nutrition rebuild must not reduce coverage."
        }

        require(
            result.after.missingCatalogItemCount <=
                    result.before.missingCatalogItemCount
        ) {
            "Nutrition rebuild must not increase missing items."
        }

        require(
            result.delta.mappingCount ==
                    result.persistence.addedMappingCount
        ) {
            "Mapping delta differs from persisted added mappings."
        }
    }

    private fun writeResult(
        result: NutritionKnowledgeRebuildResult
    ) {
        resultFile.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create nutrition rebuild " +
                                "report directory: " +
                                directory.absolutePath
                    }
                }
            }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        resultFile.writeText(
            gson.toJson(result) + "\n"
        )
    }

    private fun printResult(
        result: NutritionKnowledgeRebuildResult
    ) {
        output.println(
            "Catalog items            : " +
                    result.after.catalogItemCount
        )
        output.println()
        output.println(
            "Exact before             : " +
                    result.before.exactMatchCount
        )
        output.println(
            "Exact after              : " +
                    result.after.exactMatchCount
        )
        output.println()
        output.println(
            "Mapped before            : " +
                    result.before.mappedMatchCount
        )
        output.println(
            "Mapped after             : " +
                    result.after.mappedMatchCount
        )
        output.println()
        output.println(
            "Runtime entries before   : " +
                    result.before.runtimeEntryCount
        )
        output.println(
            "Runtime entries after    : " +
                    result.after.runtimeEntryCount
        )
        output.println()
        output.println(
            "Covered before           : " +
                    result.before.coveredCatalogItemCount
        )
        output.println(
            "Covered after            : " +
                    result.after.coveredCatalogItemCount
        )
        output.println(
            "Covered added            : " +
                    result.delta.coveredCatalogItemCount
        )
        output.println()
        output.println(
            "Coverage before          : " +
                    formatCoverage(
                        result.before.coverage
                    )
        )
        output.println(
            "Coverage after           : " +
                    formatCoverage(
                        result.after.coverage
                    )
        )
        output.println(
            "Coverage delta           : " +
                    formatPercentagePointDelta(
                        result.delta.coverage
                    )
        )
        output.println()
        output.println(
            "Missing before           : " +
                    result.before.missingCatalogItemCount
        )
        output.println(
            "Missing after            : " +
                    result.after.missingCatalogItemCount
        )
    }

    private fun formatCoverage(
        value: Double
    ): String {

        return String.format(
            java.util.Locale.ROOT,
            "%.2f%%",
            value * 100.0
        )
    }

    private fun formatPercentagePointDelta(
        value: Double
    ): String {

        return String.format(
            java.util.Locale.ROOT,
            "%+.2f percentage points",
            value * 100.0
        )
    }
}