package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.diagnostics

import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.NutritionMatchPersistenceDiagnosticReport
import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.NutritionMatchPersistenceDiagnosticReportWriter
import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.NutritionMatchPersistenceDiagnosticRunner
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildProjectFiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionMatchPersistenceDiagnosticTest {

    @Test
    fun diagnoseNonPersistedNutritionMatches() {

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
                        "nutrition.match-persistence-diagnostics.json"
            )

        val report =
            NutritionMatchPersistenceDiagnosticRunner(
                decisionFile =
                    files.decisionFile,
                validationFile =
                    files.representativeValidationFile,
                mappingFile =
                    files.outputMappingFile,
                runtimeNutritionFile =
                    files.runtimeNutritionFile,
                coverageGapReportFile =
                    coverageGapReportFile
            )
                .run()

        NutritionMatchPersistenceDiagnosticReportWriter(
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
                report.matchDecisionCount,
            actual =
                report.fullyPersistedCount +
                        report.missingPersistenceCount
        )

        assertEquals(
            expected =
                report.matchDecisionCount,
            actual =
                report.diagnostics.size
        )

        assertEquals(
            expected =
                report.expectedMatchNotPersistedCount,
            actual =
                report.missingPersistenceCount,
            message =
                "Diagnosed missing MATCH persistence must correspond " +
                        "to MATCH_NOT_PERSISTED in the coverage-gap " +
                        "report."
        )

        assertTrue(
            actual =
                outputFile.isFile,
            message =
                "Nutrition match persistence diagnostic report was " +
                        "not written: " +
                        outputFile.absolutePath
        )
    }

    private fun printReport(
        report: NutritionMatchPersistenceDiagnosticReport,
        outputFile: File
    ) {
        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("NUTRITION MATCH PERSISTENCE DIAGNOSTICS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println(
            "MATCH decisions           : " +
                    report.matchDecisionCount
        )
        println(
            "Validation records        : " +
                    report.validationRecordCount
        )
        println(
            "Explicitly accepted       : " +
                    report.explicitlyAcceptedValidationCount
        )
        println(
            "Explicitly rejected       : " +
                    report.explicitlyRejectedValidationCount
        )
        println(
            "Mappings present          : " +
                    report.mappingPresentCount
        )
        println(
            "Runtime entries present   : " +
                    report.runtimePresentCount
        )
        println(
            "Fully persisted           : " +
                    report.fullyPersistedCount
        )
        println(
            "Missing persistence       : " +
                    report.missingPersistenceCount
        )
        println(
            "Expected missing          : " +
                    (
                            report.expectedMatchNotPersistedCount
                                ?.toString()
                                ?: "not available"
                            )
        )
        println()

        report.countsByFirstMissingStage
            .forEach { (stage, count) ->

                println(
                    stage.padEnd(
                        length =
                            24
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