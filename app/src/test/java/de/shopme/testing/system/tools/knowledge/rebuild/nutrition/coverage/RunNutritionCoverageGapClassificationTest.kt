package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.coverage

import de.shopme.tools.knowledge.rebuild.nutrition.adapter.DefaultNutritionKnowledgeSnapshotReader
import de.shopme.tools.knowledge.rebuild.nutrition.coverage.NutritionCoverageGapClassifier
import de.shopme.tools.knowledge.rebuild.nutrition.coverage.NutritionCoverageGapReportWriter
import de.shopme.tools.knowledge.rebuild.nutrition.runner.NutritionKnowledgeRebuildProjectFiles
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionCoverageGapClassificationTest {

    @Test
    fun runNutritionCoverageGapClassification() {

        val projectRoot =
            findProjectRoot()

        val files =
            NutritionKnowledgeRebuildProjectFiles
                .fromProjectRoot(
                    projectRoot =
                        projectRoot
                )

        val snapshotReader =
            DefaultNutritionKnowledgeSnapshotReader(
                catalogFile =
                    files.catalogFile,
                exactMappingFile =
                    files.exactMappingFile,
                runtimeNutritionFile =
                    files.runtimeNutritionFile,
                mappingFile =
                    files.outputMappingFile
            )

        val report =
            NutritionCoverageGapClassifier(
                catalogFile =
                    files.catalogFile,
                exactMappingFile =
                    files.exactMappingFile,
                catalogServerMappingFile =
                    files.outputMappingFile,
                requestFile =
                    files.requestFile,
                decisionFile =
                    files.decisionFile,
                sourceAvailabilityFile =
                    File(
                        "../data/generated/knowledge/reports/" +
                                "nutrition.off-availability-for-no-candidates.json"
                    ),
                snapshotReader =
                    snapshotReader
            )
                .classify()

        val reportFile =
            File(
                projectRoot,
                "data/generated/knowledge/reports/" +
                        "nutrition.coverage-gaps.json"
            )

        NutritionCoverageGapReportWriter(
            outputFile =
                reportFile
        )
            .write(
                report =
                    report
            )

        printReport(
            report =
                report,
            reportFile =
                reportFile
        )

        assertEquals(
            expected =
                report.catalogItemCount,
            actual =
                report.coveredCatalogItemCount +
                        report.missingCatalogItemCount
        )

        assertEquals(
            expected =
                report.missingCatalogItemCount,
            actual =
                report.gaps.size
        )

        assertEquals(
            expected =
                0,
            actual =
                report.unclassifiedGapCount,
            message =
                "Every missing nutrition catalog key must receive a " +
                        "deterministic classification."
        )

        assertTrue(
            actual =
                reportFile.isFile,
            message =
                "Nutrition coverage gap report was not written: " +
                        reportFile.absolutePath
        )
    }

    private fun printReport(
        report:
        de.shopme.tools.knowledge.rebuild.nutrition.coverage
        .NutritionCoverageGapReport,
        reportFile: File
    ) {
        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("NUTRITION COVERAGE GAP CLASSIFICATION")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println(
            "Catalog items       : " +
                    report.catalogItemCount
        )
        println(
            "Covered             : " +
                    report.coveredCatalogItemCount
        )
        println(
            "Missing             : " +
                    report.missingCatalogItemCount
        )
        println(
            "Classified          : " +
                    report.classifiedGapCount
        )
        println(
            "Unclassified        : " +
                    report.unclassifiedGapCount
        )
        println()

        report.countsByType
            .forEach { (type, count) ->

                println(
                    type.padEnd(
                        length =
                            24
                    ) +
                            ": " +
                            count
                )
            }

        println()
        println(
            "Report              : " +
                    reportFile.absolutePath
        )
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
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