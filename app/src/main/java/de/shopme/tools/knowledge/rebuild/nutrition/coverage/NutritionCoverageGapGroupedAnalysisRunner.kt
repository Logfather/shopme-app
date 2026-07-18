package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import java.io.File

class NutritionCoverageGapGroupedAnalysisRunner(
    private val analyzer:
    NutritionCoverageGapGroupedAnalyzer =
        NutritionCoverageGapGroupedAnalyzer(),
    private val writer:
    NutritionCoverageGapGroupedAnalysisWriter =
        NutritionCoverageGapGroupedAnalysisWriter(),
    private val printLine: (String) -> Unit =
        ::println
) {

    fun run(
        coverageGapReportFile: File,
        outputFile: File
    ): NutritionCoverageGapGroupedAnalysis {

        val report =
            analyzer.analyze(
                coverageGapReportFile =
                    coverageGapReportFile
            )

        writer.write(
            report =
                report,
            outputFile =
                outputFile
        )

        printSummary(
            report =
                report,
            outputFile =
                outputFile
        )

        return report
    }

    private fun printSummary(
        report: NutritionCoverageGapGroupedAnalysis,
        outputFile: File
    ) {
        printLine("")
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        printLine(
            "NUTRITION COVERAGE GAP TYPE ANALYSIS"
        )
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        printLine(
            "Total gaps  : ${report.totalGapCount}"
        )
        printLine(
            "Type groups : ${report.typeGroupCount}"
        )
        printLine("")

        report.groups
            .forEach { group ->

                printLine(
                    group.type
                        .padEnd(34) +
                            group.count
                                .toString()
                                .padStart(4) +
                            "  " +
                            String.format(
                                "%6.2f%%",
                                group.percentage
                            )
                )
            }

        printLine("")
        printLine(
            "Output      : ${outputFile.absolutePath}"
        )
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }
}