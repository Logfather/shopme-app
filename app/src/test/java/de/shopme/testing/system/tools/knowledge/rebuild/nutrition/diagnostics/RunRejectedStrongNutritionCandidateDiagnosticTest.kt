package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.diagnostics

import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.RejectedStrongNutritionCandidateDiagnosticReport
import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.RejectedStrongNutritionCandidateDiagnosticReportWriter
import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.RejectedStrongNutritionCandidateDiagnosticRunner
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildProjectFiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunRejectedStrongNutritionCandidateDiagnosticTest {

    @Test
    fun diagnoseRejectedStrongNutritionCandidates() {

        val projectRoot =
            findProjectRoot()

        val files =
            NutritionKnowledgeRebuildProjectFiles
                .fromProjectRoot(
                    projectRoot =
                        projectRoot
                )

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
                        "nutrition.rejected-strong-" +
                        "candidate-diagnostics.json"
            )

        val report =
            RejectedStrongNutritionCandidateDiagnosticRunner(
                coverageGapReportFile =
                    coverageGapReportFile,
                requestFile =
                    files.requestFile,
                decisionFile =
                    files.decisionFile
            )
                .run()

        RejectedStrongNutritionCandidateDiagnosticReportWriter(
            outputFile =
                outputFile
        )
            .write(
                report =
                    report
            )

        printReport(
            report =
                report,
            outputFile =
                outputFile
        )

        assertEquals(
            expected =
                report.selectedGapCount,
            actual =
                report.strongTopCandidateCount +
                        report.moderateTopCandidateCount
        )

        assertEquals(
            expected =
                report.selectedGapCount,
            actual =
                report.diagnostics.size
        )

        assertTrue(
            actual =
                report.selectedGapCount >
                        0,
            message =
                "Expected rejected strong or moderate nutrition " +
                        "candidates."
        )

        assertTrue(
            actual =
                outputFile.isFile,
            message =
                "Rejected strong nutrition candidate report was not " +
                        "written: " +
                        outputFile.absolutePath
        )
    }

    private fun printReport(
        report: RejectedStrongNutritionCandidateDiagnosticReport,
        outputFile: File
    ) {
        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("REJECTED STRONG NUTRITION CANDIDATES")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println(
            "Selected gaps             : " +
                    report.selectedGapCount
        )
        println(
            "Strong top candidates     : " +
                    report.strongTopCandidateCount
        )
        println(
            "Moderate top candidates   : " +
                    report.moderateTopCandidateCount
        )
        println(
            "Representative review     : " +
                    report.representativeReviewRecommendedCount
        )
        println(
            "Conflicts                  : " +
                    report.conflictCount
        )
        println()

        report.countsByDiagnosticType
            .forEach { (type, count) ->

                println(
                    type.padEnd(
                        length =
                            36
                    ) +
                            ": " +
                            count
                )
            }

        println()
        println(
            "Report                    : " +
                    outputFile.absolutePath
        )
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun findProjectRoot():
            File {

        val userDirectory =
            requireNotNull(
                System.getProperty(
                    "user.dir"
                )
            ) {
                "System property 'user.dir' is not available."
            }

        val workingDirectory =
            File(
                userDirectory
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
}