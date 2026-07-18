package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.diagnostics

import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionScoreClusterCandidateAnalysisWriter
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionScoreClusterCandidateAnalyzer
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionScoreClusterDiagnosticGenerator
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunNutritionScoreClusterCandidateAnalysisTest {

    @Test
    fun generateNutritionScoreClusterCandidateAnalysis() {
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
                        "nutrition.score-cluster-candidate-analysis.json",
            )

        require(coverageGapFile.isFile) {
            "Nutrition coverage-gap report does not exist: " +
                    coverageGapFile.absolutePath
        }

        require(matchRequestFile.isFile) {
            "Nutrition match-request artifact does not exist: " +
                    matchRequestFile.absolutePath
        }

        require(matchDiagnosticFile.isFile) {
            "Nutrition match-diagnostic report does not exist: " +
                    matchDiagnosticFile.absolutePath
        }

        val diagnosticReport =
            NutritionScoreClusterDiagnosticGenerator()
                .generate(
                    coverageGapFile = coverageGapFile,
                    matchRequestFile = matchRequestFile,
                    matchDiagnosticFile = matchDiagnosticFile,
                )

        val analysis =
            NutritionScoreClusterCandidateAnalyzer()
                .analyze(
                    report = diagnosticReport,
                )

        NutritionScoreClusterCandidateAnalysisWriter()
            .write(
                analysis = analysis,
                outputFile = outputFile,
            )

        println(
            buildString {
                appendLine()
                appendLine(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                )
                appendLine(
                    "NUTRITION SCORE-CLUSTER CANDIDATE ANALYSIS",
                )
                appendLine(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                )
                appendLine(
                    "Entries                 : " +
                            analysis.entryCount,
                )
                appendLine(
                    "Shared-token counts     : " +
                            analysis.countsBySharedTokenCount,
                )
                appendLine(
                    "Exact token-set matches : " +
                            analysis.countsByExactTokenSetMatch,
                )
                appendLine(
                    "Containment types       : " +
                            analysis.countsByContainmentType,
                )
                appendLine(
                    "Candidate counts        : " +
                            analysis.countsByCandidateCount,
                )
                appendLine(
                    "Output                  : " +
                            outputFile.path,
                )
                appendLine(
                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                )
            },
        )

        assertEquals(
            expected = 316,
            actual = analysis.entryCount,
            message =
                "Expected one analysis entry for every SCORE_CLUSTER gap.",
        )

        assertEquals(
            expected = diagnosticReport.scoreClusterCount,
            actual = analysis.entryCount,
            message =
                "Candidate analysis must cover all diagnostic entries.",
        )

        val diagnosticCatalogKeys =
            diagnosticReport.entries
                .map { diagnosticEntry ->
                    diagnosticEntry.catalogKey
                }

        val analysisCatalogKeys =
            analysis.entries
                .map { analysisEntry ->
                    analysisEntry.catalogKey
                }

        assertEquals(
            expected = diagnosticCatalogKeys,
            actual = analysisCatalogKeys,
            message =
                "Candidate analysis must retain deterministic catalog-key ordering.",
        )

        analysis.entries.forEach { analysisEntry ->
            assertEquals(
                expected = analysisEntry.candidates.size,
                actual = analysisEntry.candidateCount,
                message =
                    "Candidate count differs for catalog key: " +
                            analysisEntry.catalogKey,
            )

            assertEquals(
                expected =
                    (1..analysisEntry.candidates.size)
                        .toList(),
                actual =
                    analysisEntry.candidates
                        .map { candidate ->
                            candidate.rank
                        },
                message =
                    "Candidate ranks are not consecutive for: " +
                            analysisEntry.catalogKey,
            )
        }

        val analyzedCandidates =
            analysis.entries
                .flatMap { analysisEntry ->
                    analysisEntry.candidates
                }

        analyzedCandidates.forEach { candidate ->
            assertEquals(
                expected = candidate.sharedTokens.size,
                actual = candidate.sharedTokenCount,
                message =
                    "Shared-token count differs for candidate: " +
                            candidate.serverKey,
            )
        }

        assertEquals(
            expected = analysis.entryCount,
            actual =
                analysis.countsByCandidateCount
                    .values
                    .sum(),
            message =
                "Candidate-count summary does not cover all entries.",
        )

        assertEquals(
            expected = analyzedCandidates.size,
            actual =
                analysis.countsBySharedTokenCount
                    .values
                    .sum(),
            message =
                "Shared-token summary does not cover all candidates.",
        )

        assertEquals(
            expected = analyzedCandidates.size,
            actual =
                analysis.countsByExactTokenSetMatch
                    .values
                    .sum(),
            message =
                "Exact-token summary does not cover all candidates.",
        )

        assertEquals(
            expected = analyzedCandidates.size,
            actual =
                analysis.countsByContainmentType
                    .values
                    .sum(),
            message =
                "Containment summary does not cover all candidates.",
        )

        assertTrue(
            actual = outputFile.isFile,
            message =
                "Candidate-analysis report was not written: " +
                        outputFile.absolutePath,
        )

        assertTrue(
            actual = outputFile.length() > 0L,
            message =
                "Candidate-analysis report is empty.",
        )
    }
}