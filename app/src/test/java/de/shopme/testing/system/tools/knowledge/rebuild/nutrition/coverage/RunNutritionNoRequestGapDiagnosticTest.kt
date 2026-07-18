package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.coverage

import de.shopme.tools.knowledge.rebuild.nutrition.coverage.NutritionNoRequestGapDiagnosticRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RunNutritionNoRequestGapDiagnosticTest {

    @Test
    fun diagnoseRemainingNutritionNoRequestGaps() {

        val projectRoot =
            findProjectRoot()

        val report =
            NutritionNoRequestGapDiagnosticRunner()
                .run(
                    coverageGapReportFile =
                        File(
                            projectRoot,
                            "data/generated/knowledge/reports/" +
                                    "nutrition.coverage-gaps.json"
                        ),
                    matchReportDirectory =
                        File(
                            projectRoot,
                            "data/generated/reports/" +
                                    "catalog-server-matches"
                        ),
                    matchRequestFile =
                        File(
                            projectRoot,
                            "data/generated/knowledge/match-requests/" +
                                    "nutrition.match-requests.json"
                        ),
                    mappingFile =
                        File(
                            projectRoot,
                            "data/generated/knowledge/mappings/" +
                                    "catalog-server.mappings.json"
                        ),
                    outputFile =
                        File(
                            projectRoot,
                            "data/generated/knowledge/reports/" +
                                    "nutrition.no-request-gap-diagnostic.json"
                        )
                )

        assertEquals(
            expected =
                EXPECTED_NO_REQUEST_GAP_COUNT,
            actual =
                report.noRequestGapCount,
            message =
                "All remaining NO_REQUEST nutrition gaps must be diagnosed."
        )

        assertEquals(
            expected =
                report.noRequestGapCount,
            actual =
                report.entries.size
        )

        assertEquals(
            expected =
                report.noRequestGapCount,
            actual =
                report.reasonCounts.values.sum()
        )

        assertEquals(
            expected =
                report.entries.size,
            actual =
                report.entries
                    .map {
                        it.catalogKey
                    }
                    .distinct()
                    .size,
            message =
                "Every NO_REQUEST catalog key must be diagnosed exactly once."
        )

        assertTrue(
            actual =
                report.entries.all {
                    it.details.isNotBlank()
                }
        )

        assertFalse(
            actual =
                report.entries.any {
                    it.requestPresent &&
                            it.reason.name !=
                            "REQUEST_PRESENT_BUT_COVERAGE_REPORT_STALE"
                },
            message =
                "Persisted requests must be reported as stale coverage data."
        )
        assertEquals(
            expected =
                setOf(
                    "chervil",
                    "mace",
                    "salsify"
                ),
            actual =
                report.entries
                    .map {
                        it.catalogKey
                    }
                    .toSet()
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

        const val EXPECTED_NO_REQUEST_GAP_COUNT =
            3
    }
}