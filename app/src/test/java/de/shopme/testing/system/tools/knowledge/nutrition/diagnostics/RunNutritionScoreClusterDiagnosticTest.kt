package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionScoreClusterDiagnosticTest {

    @Test
    fun generateNutritionScoreClusterDiagnosticReport() {
        val coverageGapFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.coverage-gaps.json",
            )

        val matchRequestFile =
            File(
                "../data/generated/knowledge/match-requests/" +
                        "nutrition.match-requests.json",
            )

        val matchDiagnosticFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.match-diagnostics.json",
            )

        val outputFile =
            File(
                "../data/generated/knowledge/reports/" +
                        "nutrition.score-cluster-diagnostics.json",
            )

        val report =
            NutritionScoreClusterDiagnosticGenerator()
                .generate(
                    coverageGapFile = coverageGapFile,
                    matchRequestFile = matchRequestFile,
                    matchDiagnosticFile =
                        matchDiagnosticFile,
                )

        NutritionScoreClusterDiagnosticReportWriter()
            .write(
                report = report,
                outputFile = outputFile,
            )

        println(
            buildString {
                appendLine()
                appendLine(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                )
                appendLine(
                    "NUTRITION SCORE-CLUSTER DIAGNOSTICS",
                )
                appendLine(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                )
                appendLine(
                    "Score clusters          : " +
                            report.scoreClusterCount,
                )
                appendLine(
                    "Requests present        : " +
                            report.requestPresentCount,
                )
                appendLine(
                    "Diagnostics present     : " +
                            report.diagnosticPresentCount,
                )
                appendLine(
                    "Average top score       : " +
                            report.averageTopScore,
                )
                appendLine(
                    "Average second score    : " +
                            report.averageSecondScore,
                )
                appendLine(
                    "Average score delta     : " +
                            report.averageScoreDelta,
                )
                appendLine(
                    "Delta buckets           : " +
                            report.countsByScoreDeltaBucket,
                )
                appendLine(
                    "Selected ranks          : " +
                            report.countsBySelectedRank,
                )
                appendLine(
                    "Decision types          : " +
                            report.countsByDecisionType,
                )
                appendLine(
                    "Decision sources        : " +
                            report.countsByDecisionSource,
                )
                appendLine(
                    "Validation statuses     : " +
                            report.countsByValidationStatus,
                )
                appendLine(
                    "Output                   : " +
                            outputFile.path,
                )
                appendLine(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                )
            },
        )

        assertTrue(
            actual = outputFile.isFile,
            message =
                "Score-cluster diagnostic report was not written.",
        )

        assertEquals(
            expected = 316,
            actual = report.scoreClusterCount,
            message =
                "Expected the current productive SCORE_CLUSTER count.",
        )

        assertEquals(
            expected = report.scoreClusterCount,
            actual = report.entries.size,
        )

        assertEquals(
            expected =
                report.entries
                    .map(
                        NutritionScoreClusterDiagnosticEntry::catalogKey,
                    )
                    .sorted(),
            actual =
                report.entries
                    .map(
                        NutritionScoreClusterDiagnosticEntry::catalogKey,
                    ),
            message =
                "Score-cluster entries must be sorted deterministically.",
        )
    }
}