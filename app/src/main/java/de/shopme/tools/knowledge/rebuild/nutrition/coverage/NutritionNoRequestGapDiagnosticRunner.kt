package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import java.io.File

class NutritionNoRequestGapDiagnosticRunner(
    private val diagnostic:
    NutritionNoRequestGapDiagnostic =
        NutritionNoRequestGapDiagnostic(),
    private val writer:
    NutritionNoRequestGapDiagnosticReportWriter =
        NutritionNoRequestGapDiagnosticReportWriter(),
    private val printLine: (String) -> Unit =
        ::println
) {

    fun run(
        coverageGapReportFile: File,
        matchReportDirectory: File,
        matchRequestFile: File,
        mappingFile: File,
        outputFile: File
    ): NutritionNoRequestGapDiagnosticReport {

        val report =
            diagnostic.diagnose(
                coverageGapReportFile =
                    coverageGapReportFile,
                matchReportDirectory =
                    matchReportDirectory,
                matchRequestFile =
                    matchRequestFile,
                mappingFile =
                    mappingFile
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
        report: NutritionNoRequestGapDiagnosticReport,
        outputFile: File
    ) {
        printLine("")
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        printLine(
            "NUTRITION NO-REQUEST GAP DIAGNOSTIC"
        )
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        printLine(
            "NO_REQUEST gaps : ${report.noRequestGapCount}"
        )
        printLine("")

        report.reasonCounts
            .forEach { (
                           reason,
                           count
                       ) ->
                printLine(
                    reason.padEnd(48) +
                            count.toString().padStart(4)
                )
            }

        printLine("")

        report.entries.forEach { entry ->
            printLine(
                "${entry.catalogKey} → ${entry.reason}"
            )
        }

        printLine("")
        printLine(
            "Output          : ${outputFile.absolutePath}"
        )
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }
}