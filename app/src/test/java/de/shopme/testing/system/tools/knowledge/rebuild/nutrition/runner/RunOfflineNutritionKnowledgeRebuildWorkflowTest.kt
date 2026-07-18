package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.runner

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildProjectFiles
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildWorkflowFactory
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunOfflineNutritionKnowledgeRebuildWorkflowTest {

    @Test
    fun runOfflineNutritionKnowledgeRebuild() {

        val projectRoot =
            File("..")

        val files =
            NutritionKnowledgeRebuildProjectFiles
                .fromProjectRoot(
                    projectRoot =
                        projectRoot
                )

        val result =
            NutritionKnowledgeRebuildWorkflowFactory()
                .create(
                    mode =
                        NutritionKnowledgeRebuildMode.OFFLINE,
                    files =
                        files
                )
                .run(
                    mode =
                        NutritionKnowledgeRebuildMode.OFFLINE
                )

        assertEquals(
            expected =
                NutritionKnowledgeRebuildMode.OFFLINE,
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
                result.matching.chatGptDecisionCount
        )

        assertEquals(
            expected = 0,
            actual =
                result.matching.errorCount
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

        val persisted =
            JsonParser.parseString(
                files.rebuildResultFile.readText()
            )
                .asJsonObject

        assertEquals(
            expected = "OFFLINE",
            actual =
                persisted["mode"]
                    .asString
        )

        /*
         * Interne Konsistenz
         */

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

        /*
         * Produktive Regression
         */

        assertEquals(
            expected = 2709,
            actual =
                result.after.catalogItemCount
        )

        assertEquals(
            expected = 1496,
            actual =
                result.after.exactMatchCount
        )

        assertEquals(
            expected = 477,
            actual =
                result.after.mappingCount
        )

        assertEquals(
            expected = 477,
            actual =
                result.after.mappedMatchCount
        )

        assertEquals(
            expected = 1973,
            actual =
                result.after.runtimeEntryCount
        )

        assertEquals(
            expected = 1973,
            actual =
                result.after.coveredCatalogItemCount
        )

        assertEquals(
            expected = 736,
            actual =
                result.after.missingCatalogItemCount
        )

        /*
         * Idempotenz-Regression
         *
         * Ein erneuter Offline-Rebuild darf den Bestand
         * weder erweitern noch reduzieren.
         */

        assertEquals(
            expected =
                result.before.mappingCount,
            actual =
                result.after.mappingCount
        )

        assertEquals(
            expected =
                result.before.coveredCatalogItemCount,
            actual =
                result.after.coveredCatalogItemCount
        )

        assertEquals(
            expected =
                result.before.missingCatalogItemCount,
            actual =
                result.after.missingCatalogItemCount
        )

        assertEquals(
            expected =
                result.before.coverage,
            actual =
                result.after.coverage
        )

        assertEquals(
            expected = 0,
            actual =
                result.delta.mappingCount
        )

        assertEquals(
            expected = 0,
            actual =
                result.delta.coveredCatalogItemCount
        )

        assertEquals(
            expected = 0,
            actual =
                result.delta.missingCatalogItemCount
        )

        assertTrue(
            abs(
                result.delta.coverage
            ) < 1e-12
        )

        println()

        println(
            "Nutrition mappings before=" +
                    result.before.mappingCount
        )

        println(
            "Nutrition mappings after=" +
                    result.after.mappingCount
        )

        println(
            "Nutrition mappings added=" +
                    result.delta.mappingCount
        )

        println(
            "Nutrition covered before=" +
                    result.before.coveredCatalogItemCount
        )

        println(
            "Nutrition covered after=" +
                    result.after.coveredCatalogItemCount
        )

        println(
            "Nutrition missing before=" +
                    result.before.missingCatalogItemCount
        )

        println(
            "Nutrition missing after=" +
                    result.after.missingCatalogItemCount
        )

        println(
            "Local model decisions=" +
                    result.matching.localModelDecisionCount
        )

        println(
            "ChatGPT decisions=" +
                    result.matching.chatGptDecisionCount
        )

        println(
            "GPT fallback required=" +
                    result.matching.gptFallbackRequiredCount
        )

        println(
            "Nutrition rebuild report=" +
                    files.rebuildResultFile.path
        )

        println(
            "Nutrition catalog items=" +
                    result.after.catalogItemCount
        )

        println(
            "Nutrition exact matches=" +
                    result.after.exactMatchCount
        )

        println(
            "Nutrition mapped matches=" +
                    result.after.mappedMatchCount
        )

        println(
            "Nutrition runtime entries=" +
                    result.after.runtimeEntryCount
        )

        println(
            "Nutrition coverage=" +
                    result.after.coverage
        )
    }
}