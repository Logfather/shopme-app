package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.diagnostics

import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.NutritionSpecializationRiskType
import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.RejectedStrongNutritionCandidateDiagnosticRunner
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RejectedStrongNutritionCandidateSpecializationRiskTest {

    @Test
    fun rejectNutritionRelevantSpecializationsFromRepresentativeReview() {

        val directory =
            Files.createTempDirectory(
                "nutrition-specialization-risk"
            )
                .toFile()

        try {
            val coverageFile =
                File(
                    directory,
                    "nutrition.coverage-gaps.json"
                )

            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )

            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )

            coverageFile.writeText(
                """
                {
                  "version": 1,
                  "gaps": [
                    {
                      "catalogKey": "organic ribbon pasta",
                      "type": "NO_MATCH",
                      "noMatchCause": "STRONG_TOP_CANDIDATE_REJECTED",
                      "topCandidateKey": "organic edamame ribbon pasta"
                    },
                    {
                      "catalogKey": "organic egg noodles",
                      "type": "NO_MATCH",
                      "noMatchCause": "STRONG_TOP_CANDIDATE_REJECTED",
                      "topCandidateKey": "organic wide egg noodles"
                    }
                  ]
                }
                """.trimIndent()
            )

            requestFile.writeText(
                """
                {
                  "version": 1,
                  "requests": [
                    {
                      "catalogKey": "organic ribbon pasta",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "organic edamame ribbon pasta",
                          "diagnosticScore": 0.90,
                          "sharedTokens": [
                            "organic",
                            "pasta",
                            "ribbon"
                          ]
                        }
                      ]
                    },
                    {
                      "catalogKey": "organic egg noodles",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "organic wide egg noodles",
                          "diagnosticScore": 0.90,
                          "sharedTokens": [
                            "egg",
                            "noodles",
                            "organic"
                          ]
                        }
                      ]
                    }
                  ]
                }
                """.trimIndent()
            )

            decisionFile.writeText(
                """
                {
                  "version": 1,
                  "decisions": [
                    {
                      "catalogKey": "organic ribbon pasta",
                      "serverArtifact": "nutrition.json",
                      "type": "NO_MATCH",
                      "confidence": 0.90,
                      "reason": "Candidate adds edamame.",
                      "decisionSource": "CHAT_GPT"
                    },
                    {
                      "catalogKey": "organic egg noodles",
                      "serverArtifact": "nutrition.json",
                      "type": "NO_MATCH",
                      "confidence": 0.90,
                      "reason": "Candidate specifies wide noodles.",
                      "decisionSource": "CHAT_GPT"
                    }
                  ]
                }
                """.trimIndent()
            )

            val report =
                RejectedStrongNutritionCandidateDiagnosticRunner(
                    coverageGapReportFile =
                        coverageFile,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile
                )
                    .run()

            val edamame =
                report.diagnostics.single {
                    it.catalogKey ==
                            "organic ribbon pasta"
                }

            assertFalse(
                actual =
                    edamame.representativeReviewRecommended
            )

            assertEquals(
                expected =
                    listOf(
                        NutritionSpecializationRiskType
                            .INGREDIENT_OR_SUBSTRATE
                    ),
                actual =
                    edamame.specializationRiskTypes
            )

            assertEquals(
                expected =
                    listOf(
                        "edamame"
                    ),
                actual =
                    edamame.highRiskAdditionalTokens
            )

            val wide =
                report.diagnostics.single {
                    it.catalogKey ==
                            "organic egg noodles"
                }

            assertTrue(
                actual =
                    wide.representativeReviewRecommended
            )

            assertEquals(
                expected =
                    listOf(
                        NutritionSpecializationRiskType
                            .NON_CRITICAL_STYLE
                    ),
                actual =
                    wide.specializationRiskTypes
            )

        } finally {

            directory.deleteRecursively()
        }
    }
}