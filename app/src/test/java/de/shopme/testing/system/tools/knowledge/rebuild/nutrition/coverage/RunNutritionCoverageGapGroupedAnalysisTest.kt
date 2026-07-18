package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.coverage

import de.shopme.tools.knowledge.rebuild.nutrition.coverage.NutritionCoverageGapGroupedAnalysisRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionCoverageGapGroupedAnalysisTest {

    @Test
    fun analyzeRemainingNutritionCoverageGapsByType() {

        val projectRoot =
            findProjectRoot()

        val coverageGapReportFile =
            File(
                projectRoot,
                "data/generated/knowledge/reports/" +
                        "nutrition.coverage-gaps.json"
            )

        val outputFile =
            File(
                projectRoot,
                "data/generated/knowledge/reports/" +
                        "nutrition.coverage-gap-type-analysis.json"
            )

        val report =
            NutritionCoverageGapGroupedAnalysisRunner()
                .run(
                    coverageGapReportFile =
                        coverageGapReportFile,
                    outputFile =
                        outputFile
                )

        assertEquals(
            expected =
                CURRENT_EXPECTED_GAP_COUNT,
            actual =
                report.totalGapCount,
            message =
                "The grouped analysis must cover the current 693 " +
                        "nutrition coverage gaps."
        )

        assertEquals(
            expected =
                report.totalGapCount,
            actual =
                report.groups.sumOf {
                    it.count
                }
        )

        assertEquals(
            expected =
                report.groups.size,
            actual =
                report.typeGroupCount
        )

        assertTrue(
            actual =
                report.groups.isNotEmpty(),
            message =
                "The grouped coverage-gap analysis must contain groups."
        )

        assertTrue(
            actual =
                report.groups.all {
                    it.examples.size <= 10
                },
            message =
                "Every type group may contain at most ten examples."
        )

        assertEquals(
            expected =
                report.groups.sortedWith(
                    compareByDescending<
                            de.shopme.tools.knowledge.rebuild.nutrition
                            .coverage.NutritionCoverageGapTypeAnalysis
                            > {
                        it.count
                    }
                        .thenBy {
                            it.type
                        }
                ),
            actual =
                report.groups,
            message =
                "Coverage-gap groups must be sorted deterministically."
        )

        assertTrue(
            actual =
                outputFile.isFile,
            message =
                "Grouped coverage-gap analysis file was not written."
        )
    }

    private fun findProjectRoot():
            File {

        val workingDirectory =
            File(
                requireNotNull(
                    System.getProperty(
                        "user.dir"
                    )
                )
            )
                .absoluteFile

        return generateSequence(
            seed =
                workingDirectory
        ) {
            it.parentFile
        }
            .firstOrNull { candidate ->

                File(
                    candidate,
                    "app"
                )
                    .isDirectory &&
                        File(
                            candidate,
                            "data"
                        )
                            .isDirectory
            }
            ?: error(
                "Could not locate ShopMe project root from: " +
                        workingDirectory.absolutePath
            )
    }

    private companion object {

        const val CURRENT_EXPECTED_GAP_COUNT =
            693
    }
}