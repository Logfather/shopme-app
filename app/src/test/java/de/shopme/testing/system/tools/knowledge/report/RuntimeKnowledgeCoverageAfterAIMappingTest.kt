package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.report.CoverageDimension
import de.shopme.tools.report.RuntimeKnowledgeCoverageCalculator
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RuntimeKnowledgeCoverageAfterAIMappingTest {

    @Test
    fun reportsRuntimeCoverageAfterValidatedAIMappings() {

        val assetDirectory =
            File(
                "src/main/assets"
            )

        assertTrue(
            assetDirectory.isDirectory,
            "App asset directory missing: " +
                    assetDirectory.absolutePath
        )

        val calculator =
            RuntimeKnowledgeCoverageCalculator(
                readAssetText = { assetPath ->

                    val assetFile =
                        File(
                            assetDirectory,
                            assetPath
                        )

                    require(assetFile.isFile) {
                        "Runtime asset missing: " +
                                assetFile.absolutePath
                    }

                    assetFile.readText()
                }
            )

        val entries =
            calculator.calculate(
                total = CATALOG_KEY_COUNT
            )

        val nutritionDimensionName =
            CoverageDimension.NUTRITION.displayName

        val nutrition =
            entries.singleOrNull { entry ->
                entry.name ==
                        nutritionDimensionName
            }
                ?: error(
                    "Nutrition coverage entry missing. " +
                            "Expected name='$nutritionDimensionName', " +
                            "available names=" +
                            entries.joinToString(
                                prefix = "[",
                                postfix = "]"
                            ) {
                                it.name
                            }
                )

        assertEquals(
            CATALOG_KEY_COUNT,
            nutrition.total
        )

        assertEquals(
            EXPECTED_NUTRITION_RUNTIME_ENTRIES,
            nutrition.covered,
            "Nutrition coverage does not contain all validated AI mappings"
        )

        assertEquals(
            EXPECTED_NUTRITION_AI_UPLIFT,
            nutrition.covered -
                    NUTRITION_EXACT_MATCH_COUNT
        )

        assertEquals(
            EXPECTED_NUTRITION_MISSING,
            nutrition.total -
                    nutrition.covered
        )

        val coveragePercent =
            nutrition.covered
                .toDouble()
                .div(nutrition.total)
                .times(100.0)

        assertTrue(
            coveragePercent >
                    EXACT_ONLY_COVERAGE_PERCENT,
            "Nutrition coverage must exceed exact-only coverage"
        )

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("KNOWLEDGE COVERAGE AFTER AI MAPPING")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Dimension         : $nutritionDimensionName")
        println("Catalog keys      : ${nutrition.total}")
        println("Nutrition exact   : $NUTRITION_EXACT_MATCH_COUNT")
        println("Nutrition mapped  : $EXPECTED_NUTRITION_AI_UPLIFT")
        println("Nutrition covered : ${nutrition.covered}")
        println(
            "Nutrition missing : " +
                    (nutrition.total - nutrition.covered)
        )
        println(
            "Nutrition coverage: " +
                    "%.2f".format(
                        coveragePercent
                    ) +
                    " %"
        )
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }


    companion object {

        private const val CATALOG_KEY_COUNT =
            2709

        private const val NUTRITION_EXACT_MATCH_COUNT =
            1496

        private const val EXPECTED_NUTRITION_AI_UPLIFT =
            440

        private const val EXPECTED_NUTRITION_RUNTIME_ENTRIES =
            1936

        private const val EXPECTED_NUTRITION_MISSING =
            773

        private const val EXACT_ONLY_COVERAGE_PERCENT =
            55.22
    }
}