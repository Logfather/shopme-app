package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionScoreClusterDiagnosticGenerator
import de.shopme.testing.system.tools.knowledge.nutrition.diagnostics.NutritionScoreDeltaBucket
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NutritionScoreClusterDiagnosticGeneratorTest {

    @Test
    fun generatesDeterministicScoreClusterDiagnostics() {
        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-score-cluster-diagnostic",
            )
                .toFile()

        val coverageGapFile =
            File(
                directory,
                "nutrition.coverage-gaps.json",
            )

        val matchRequestFile =
            File(
                directory,
                "nutrition.match-requests.json",
            )

        val matchDiagnosticFile =
            File(
                directory,
                "nutrition.match-diagnostics.json",
            )

        coverageGapFile.writeText(
            """
            {
              "version": 1,
              "gaps": [
                {
                  "catalogKey": "apple juice",
                  "type": "SCORE_CLUSTER",
                  "topCandidateKey": "juice apple",
                  "topCandidateScore": 0.81,
                  "secondCandidateKey": "organic apple juice",
                  "secondCandidateScore": 0.79,
                  "topScoreDelta": 0.02,
                  "topCandidateSharedTokens": [
                    "apple",
                    "juice"
                  ]
                },
                {
                  "catalogKey": "missing herb",
                  "type": "NO_MATCH"
                }
              ]
            }
            """.trimIndent(),
        )

        matchRequestFile.writeText(
            """
            {
              "version": 1,
              "requests": [
                {
                  "catalogKey": "apple juice",
                  "candidates": [
                    {
                      "serverKey": "juice apple",
                      "diagnosticScore": 0.81,
                      "sharedTokens": [
                        "apple",
                        "juice"
                      ]
                    },
                    {
                      "serverKey": "organic apple juice",
                      "diagnosticScore": 0.79,
                      "sharedTokens": [
                        "apple",
                        "juice"
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent(),
        )

        matchDiagnosticFile.writeText(
            """
            {
              "version": 1,
              "entries": [
                {
                  "catalogKey": "apple juice",
                  "selectedServerKey": "organic apple juice",
                  "decisionType": "REPRESENTATIVE",
                  "decisionConfidence": 0.88,
                  "decisionSource": "CHAT_GPT",
                  "validationStatus": "ACCEPTED",
                  "decisionReason": "Valid representative nutrition match.",
                  "validationReason": "Representative mapping accepted."
                }
              ]
            }
            """.trimIndent(),
        )

        val report =
            NutritionScoreClusterDiagnosticGenerator()
                .generate(
                    coverageGapFile = coverageGapFile,
                    matchRequestFile = matchRequestFile,
                    matchDiagnosticFile =
                        matchDiagnosticFile,
                )

        assertEquals(
            expected = 1,
            actual = report.scoreClusterCount,
        )

        assertEquals(
            expected = 1,
            actual = report.requestPresentCount,
        )

        assertEquals(
            expected = 1,
            actual = report.diagnosticPresentCount,
        )

        assertEquals(
            expected =
                mapOf(
                    NutritionScoreDeltaBucket.EXACT_TIE to 0,
                    NutritionScoreDeltaBucket.VERY_CLOSE to 1,
                    NutritionScoreDeltaBucket.CLOSE to 0,
                    NutritionScoreDeltaBucket.MODERATE to 0,
                    NutritionScoreDeltaBucket.CLEAR to 0,
                    NutritionScoreDeltaBucket.UNKNOWN to 0,
                ),
            actual =
                report.countsByScoreDeltaBucket,
        )

        val entry =
            report.entries.single()

        assertEquals(
            expected = "apple juice",
            actual = entry.catalogKey,
        )

        assertEquals(
            expected = 2,
            actual = entry.candidateCount,
        )

        assertEquals(
            expected = "juice apple",
            actual = entry.topCandidateKey,
        )

        assertEquals(
            expected = "organic apple juice",
            actual = entry.secondCandidateKey,
        )

        assertEquals(
            expected = "organic apple juice",
            actual = entry.selectedCandidateKey,
        )

        assertEquals(
            expected = 2,
            actual = entry.selectedRank,
        )

        assertEquals(
            expected =
                NutritionScoreDeltaBucket.VERY_CLOSE,
            actual = entry.scoreDeltaBucket,
        )

        assertTrue(entry.requestPresent)
        assertTrue(entry.diagnosticPresent)

        assertFalse(
            report.entries.any { candidate ->
                candidate.catalogKey == "missing herb"
            },
        )
    }
}