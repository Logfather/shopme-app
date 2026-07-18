package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.runner

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildEnvironment
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildProjectFiles
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildWorkflowFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunProductiveNutritionKnowledgeRebuildWorkflowTest {

    @Test
    fun runProductiveNutritionKnowledgeRebuild() {

        val environment =
            System.getenv()

        NutritionKnowledgeRebuildEnvironment
            .requireProductiveOpenAIEnabled(
                mode =
                    NutritionKnowledgeRebuildMode.PRODUCTIVE,
                environment =
                    environment
            )

        require(
            environment["OPENAI_API_KEY"]
                ?.isNotBlank() ==
                    true
        ) {
            "OPENAI_API_KEY is required for the productive " +
                    "nutrition knowledge rebuild."
        }

        val projectRoot =
            File("..")

        val files =
            NutritionKnowledgeRebuildProjectFiles
                .fromProjectRoot(
                    projectRoot =
                        projectRoot
                )

        val beforeMappingFileContent =
            files.outputMappingFile
                .takeIf {
                    it.isFile
                }
                ?.readText()

        try {
            val result =
                NutritionKnowledgeRebuildWorkflowFactory()
                    .create(
                        mode =
                            NutritionKnowledgeRebuildMode.PRODUCTIVE,
                        files =
                            files
                    )
                    .run(
                        mode =
                            NutritionKnowledgeRebuildMode.PRODUCTIVE
                    )

            assertEquals(
                expected =
                    NutritionKnowledgeRebuildMode.PRODUCTIVE,
                actual =
                    result.mode
            )

            assertTrue(
                files.rebuildResultFile.isFile,
                "Nutrition rebuild report was not created: " +
                        files.rebuildResultFile.absolutePath
            )

            assertTrue(
                files.runtimeNutritionFile.isFile,
                "Runtime nutrition artifact was not created: " +
                        files.runtimeNutritionFile.absolutePath
            )

            assertEquals(
                expected = 0,
                actual =
                    result.matching.gptFallbackRequiredCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.matching.errorCount
            )

            assertEquals(
                expected =
                    result.matching.processedCount,
                actual =
                    result.matching.localModelDecisionCount +
                            result.matching.chatGptDecisionCount +
                            result.matching.errorCount
            )

            assertEquals(
                expected =
                    result.matching.requestCount,
                actual =
                    result.matching.previouslyCompletedCount +
                            result.matching.processedCount
            )

            assertEquals(
                expected =
                    result.matching.processedCount,
                actual =
                    result.matching.localModelDecisionCount +
                            result.matching.chatGptDecisionCount
            )

            assertTrue(
                result.after.mappingCount >=
                        result.before.mappingCount
            )

            assertTrue(
                result.after.coveredCatalogItemCount >=
                        result.before.coveredCatalogItemCount
            )

            assertTrue(
                result.after.missingCatalogItemCount <=
                        result.before.missingCatalogItemCount
            )

            assertEquals(
                expected =
                    result.after.runtimeEntryCount,
                actual =
                    result.after.coveredCatalogItemCount
            )

            assertEquals(
                expected =
                    result.after.exactMatchCount +
                            result.after.mappedMatchCount,
                actual =
                    result.after.coveredCatalogItemCount
            )

            assertEquals(
                expected =
                    result.after.catalogItemCount -
                            result.after.coveredCatalogItemCount,
                actual =
                    result.after.missingCatalogItemCount
            )

            val persisted =
                JsonParser.parseString(
                    files.rebuildResultFile.readText()
                )
                    .asJsonObject

            assertEquals(
                expected =
                    "PRODUCTIVE",
                actual =
                    persisted["mode"]
                        .asString
            )

            println()
            println(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            )
            println(
                "PRODUCTIVE NUTRITION REBUILD RESULT"
            )
            println(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            )
            println(
                "Requests                  : " +
                        result.matching.requestCount
            )
            println(
                "Previously completed      : " +
                        result.matching.previouslyCompletedCount
            )
            println(
                "Processed                 : " +
                        result.matching.processedCount
            )
            println(
                "LOCAL_MODEL decisions     : " +
                        result.matching.localModelDecisionCount
            )
            println(
                "CHAT_GPT decisions        : " +
                        result.matching.chatGptDecisionCount
            )
            println(
                "GPT fallback required     : " +
                        result.matching.gptFallbackRequiredCount
            )
            println(
                "Errors                    : " +
                        result.matching.errorCount
            )
            println()
            println(
                "Mappings before           : " +
                        result.before.mappingCount
            )
            println(
                "Mappings after            : " +
                        result.after.mappingCount
            )
            println(
                "Mappings added            : " +
                        result.delta.mappingCount
            )
            println()
            println(
                "Covered before            : " +
                        result.before.coveredCatalogItemCount
            )
            println(
                "Covered after             : " +
                        result.after.coveredCatalogItemCount
            )
            println(
                "Covered added             : " +
                        result.delta.coveredCatalogItemCount
            )
            println()
            println(
                "Missing before            : " +
                        result.before.missingCatalogItemCount
            )
            println(
                "Missing after             : " +
                        result.after.missingCatalogItemCount
            )
            println()
            println(
                "Coverage before           : " +
                        result.before.coverage
            )
            println(
                "Coverage after            : " +
                        result.after.coverage
            )
            println(
                "Coverage delta            : " +
                        result.delta.coverage
            )
            println()
            println(
                "Report                    : " +
                        files.rebuildResultFile.path
            )
            println(
                "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            )

        } catch (
            throwable: Throwable
        ) {

            /*
             * Ein produktiver Lauf kann nach einem Teil der
             * OpenAI-Aufrufe abbrechen. Die Decision-Datei wird durch
             * den Runner fortlaufend persistiert und erlaubt daher
             * grundsätzlich eine Wiederaufnahme.
             *
             * Mapping- und Runtime-Dateien werden bei einem Fehler
             * auf ihren vorherigen Stand zurückgesetzt, damit kein
             * teilweise aufgebauter Runtime-Bestand bestehen bleibt.
             */
            restoreFile(
                file =
                    files.outputMappingFile,
                previousContent =
                    beforeMappingFileContent
            )

            throw throwable
        }
    }

    private fun restoreFile(
        file: File,
        previousContent: String?
    ) {
        if (previousContent == null) {

            if (file.exists()) {
                check(file.delete()) {
                    "Could not delete newly created file after " +
                            "failed productive rebuild: " +
                            file.absolutePath
                }
            }

            return
        }

        file.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create restore directory: " +
                                directory.absolutePath
                    }
                }
            }

        file.writeText(
            previousContent
        )
    }
}