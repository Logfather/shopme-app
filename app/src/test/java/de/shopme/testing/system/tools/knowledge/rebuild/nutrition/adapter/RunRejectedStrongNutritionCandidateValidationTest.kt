package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import de.shopme.tools.knowledge.rebuild.nutrition.adapter.RejectedStrongNutritionCandidateValidationReport
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.RejectedStrongNutritionCandidateValidationReportWriter
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.RejectedStrongNutritionCandidateValidationRunner
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunRejectedStrongNutritionCandidateValidationTest {

    @Test
    fun validateRejectedStrongNutritionCandidatesRepresentatively() {

        val projectRoot =
            findProjectRoot()

        val diagnosticReportFile =
            File(
                projectRoot,
                "data/generated/knowledge/reports/" +
                        "nutrition.rejected-strong-" +
                        "candidate-diagnostics.json"
            )

        val outputFile =
            File(
                projectRoot,
                "data/generated/knowledge/reports/" +
                        "nutrition.rejected-strong-" +
                        "candidate-validation.json"
            )

        val report =
            RejectedStrongNutritionCandidateValidationRunner(
                diagnosticReportFile =
                    diagnosticReportFile
            )
                .run()

        RejectedStrongNutritionCandidateValidationReportWriter(
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
                EXPECTED_REVIEW_CANDIDATE_COUNT,
            actual =
                report.candidateCount,
            message =
                "The deterministic specialization-risk diagnostic " +
                        "should currently expose exactly three " +
                        "representative review candidates."
        )

        assertEquals(
            expected =
                report.candidateCount,
            actual =
                report.acceptedCount +
                        report.rejectedCount
        )

        assertEquals(
            expected =
                report.candidateCount,
            actual =
                report.entries.size
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
                "Validation report contains duplicate catalog keys."
        )

        assertTrue(
            actual =
                outputFile.isFile,
            message =
                "Rejected strong nutrition candidate validation " +
                        "report was not written: " +
                        outputFile.absolutePath
        )
    }

    private fun printReport(
        report:
        RejectedStrongNutritionCandidateValidationReport,
        outputFile: File
    ) {
        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("REJECTED STRONG NUTRITION VALIDATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println(
            "Candidates                 : " +
                    report.candidateCount
        )
        println(
            "Accepted                   : " +
                    report.acceptedCount
        )
        println(
            "Rejected                   : " +
                    report.rejectedCount
        )
        println()

        report.entries
            .forEach { entry ->

                println(
                    entry.catalogKey +
                            " -> " +
                            entry.selectedServerKey
                )

                println(
                    "  diagnosticType : " +
                            entry.diagnosticType
                )

                println(
                    "  decisionType   : " +
                            entry.decisionType
                )

                println(
                    "  accepted       : " +
                            entry.accepted
                )

                println(
                    "  reasons        : " +
                            entry.reasons
                                .joinToString {
                                    it.name
                                }
                )
            }

        println()
        println(
            "Report                     : " +
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

    private companion object {

        const val EXPECTED_REVIEW_CANDIDATE_COUNT =
            3
    }
}