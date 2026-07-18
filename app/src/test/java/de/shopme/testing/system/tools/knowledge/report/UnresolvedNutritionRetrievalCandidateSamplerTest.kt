package de.shopme.testing.system.tools.knowledge.report

import de.shopme.tools.knowledge.report.UnresolvedNutritionRetrievalCandidateSampler
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UnresolvedNutritionRetrievalCandidateSamplerTest {

    @Test
    fun sampleUnresolvedNutritionRetrievalCandidates() {

        val directory =
            createTempDirectory(
                prefix =
                    "unresolved-nutrition-candidate-sample-"
            )
                .toFile()

        try {
            val inputFile =
                File(
                    directory,
                    "nutrition.unresolved-retrieval-failures.json"
                )
                    .apply {
                        writeText(
                            createFixture()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "nutrition.unresolved-retrieval-candidate-sample.json"
                )

            val outputBytes =
                ByteArrayOutputStream()

            val result =
                PrintStream(
                    outputBytes,
                    true,
                    Charsets.UTF_8.name()
                )
                    .use { output ->
                        UnresolvedNutritionRetrievalCandidateSampler(
                            highSampleLimit = 2,
                            mediumSampleLimit = 2,
                            lowSampleLimit = 2,
                            missingSampleLimit = 2
                        )
                            .run(
                                unresolvedFailureFile =
                                    inputFile,
                                outputFile =
                                    outputFile,
                                output =
                                    output
                            )
                    }

            val report =
                result.report

            assertEquals(
                expected = 8,
                actual =
                    report.summary.unresolvedSourceCount
            )

            assertEquals(
                expected = 7,
                actual =
                    report.summary.sampledCount
            )

            assertEquals(
                expected =
                    mapOf(
                        "HIGH" to 3,
                        "MEDIUM" to 2,
                        "LOW" to 1,
                        "MISSING" to 2
                    ),
                actual =
                    report.summary.sourceCountsByScoreBand
            )

            assertEquals(
                expected =
                    mapOf(
                        "HIGH" to 2,
                        "MEDIUM" to 2,
                        "LOW" to 1,
                        "MISSING" to 2
                    ),
                actual =
                    report.summary.sampledCountsByScoreBand
            )

            val highSamples =
                report.samples.filter {
                    it.scoreBand == "HIGH"
                }

            assertEquals(
                expected = 2,
                actual = highSamples.size
            )

            assertTrue(
                highSamples.all {
                    it.candidates.size == 2
                }
            )

            assertEquals(
                expected =
                    listOf(
                        1,
                        2
                    ),
                actual =
                    highSamples
                        .first()
                        .candidates
                        .map {
                            it.rank
                        }
            )

            assertTrue(
                outputFile.isFile
            )

            assertTrue(
                outputFile.length() > 0L
            )

            assertTrue(
                outputBytes
                    .toString(
                        Charsets.UTF_8.name()
                    )
                    .contains(
                        "UNRESOLVED NUTRITION RETRIEVAL CANDIDATE SAMPLE"
                    )
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun sampleUnresolvedCandidatesDeterministically() {

        val directory =
            createTempDirectory(
                prefix =
                    "unresolved-candidate-sample-deterministic-"
            )
                .toFile()

        try {
            val inputFile =
                File(
                    directory,
                    "input.json"
                )
                    .apply {
                        writeText(
                            createFixture()
                        )
                    }

            val outputFile =
                File(
                    directory,
                    "output.json"
                )

            val sampler =
                UnresolvedNutritionRetrievalCandidateSampler(
                    highSampleLimit = 2,
                    mediumSampleLimit = 1,
                    lowSampleLimit = 1,
                    missingSampleLimit = 1
                )

            val first =
                sampler.run(
                    unresolvedFailureFile =
                        inputFile,
                    outputFile =
                        outputFile,
                    output =
                        PrintStream(
                            ByteArrayOutputStream()
                        )
                )

            val firstContent =
                outputFile.readText()

            val second =
                sampler.run(
                    unresolvedFailureFile =
                        inputFile,
                    outputFile =
                        outputFile,
                    output =
                        PrintStream(
                            ByteArrayOutputStream()
                        )
                )

            assertEquals(
                expected =
                    first.report,
                actual =
                    second.report
            )

            assertEquals(
                expected =
                    firstContent,
                actual =
                    outputFile.readText()
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    private fun createFixture(): String =
        """
        {
          "version": 1,
          "thresholds": {},
          "summary": {},
          "failures": [
            ${failure("high alpha", "HIGH", 0.90)},
            ${failure("high beta", "HIGH", 0.85)},
            ${failure("high gamma", "HIGH", 0.80)},
            ${failure("medium alpha", "MEDIUM", 0.70)},
            ${failure("medium beta", "MEDIUM", 0.65)},
            ${failure("low alpha", "LOW", 0.40)},
            ${failure("missing alpha", "MISSING", null)},
            ${failure("missing beta", "MISSING", null)}
          ]
        }
        """.trimIndent()

    private fun failure(
        catalogKey: String,
        scoreBand: String,
        topScore: Double?
    ): String {

        val topScoreJson =
            topScore?.toString()
                ?: "null"

        val secondScoreJson =
            topScore
                ?.minus(0.10)
                ?.toString()
                ?: "null"

        val deltaJson =
            topScore
                ?.let {
                    "0.10"
                }
                ?: "null"

        return """
        {
          "catalogKey": "$catalogKey",
          "validationStatus": "REJECTED_NO_MATCH",
          "decisionType": "NO_MATCH",
          "decisionConfidence": 0.90,
          "scoreBand": "$scoreBand",
          "scoreDeltaBand": "MEDIUM",
          "catalogKeyLengthBand": "SHORT",
          "metrics": {
            "candidateCount": 2,
            "topCandidateScore": $topScoreJson,
            "secondCandidateScore": $secondScoreJson,
            "topScoreDelta": $deltaJson,
            "maximumSharedTokenCount": 2,
            "catalogTokenCount": 2,
            "topCandidateTokenCount": 2,
            "topCandidateTokenRatio": 1.0
          },
          "candidates": [
            {
              "rank": 1,
              "serverKey": "$catalogKey first candidate",
              "diagnosticScore": 0.70,
              "sharedTokens": [
                "alpha"
              ],
              "selected": false
            },
            {
              "rank": 2,
              "serverKey": "$catalogKey second candidate",
              "diagnosticScore": 0.60,
              "sharedTokens": [
                "candidate"
              ],
              "selected": false
            }
          ]
        }
        """.trimIndent()
    }
}