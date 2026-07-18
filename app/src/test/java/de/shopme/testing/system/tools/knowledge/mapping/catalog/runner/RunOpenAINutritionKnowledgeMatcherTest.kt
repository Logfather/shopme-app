package de.shopme.testing.system.tools.knowledge.mapping.catalog.runner

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatcher
import de.shopme.tools.knowledge.mapping.catalog.runner.RunOpenAINutritionKnowledgeMatcher
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RunOpenAINutritionKnowledgeMatcherTest {

    @Test
    fun persistsProgressAndContinuesFailedRequestsOnNextRun() {

        val directory =
            createTempDirectory(
                prefix =
                    "openai-nutrition-runner"
            ).toFile()

        try {
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

            val errorFile =
                File(
                    directory,
                    "nutrition.match-errors.json"
                )

            writeRequestFixture(
                file = requestFile
            )

            val firstMatcher =
                RecordingMatcher(
                    failingCatalogKeys =
                        setOf(
                            "banana yogurt"
                        )
                )

            val firstRun =
                RunOpenAINutritionKnowledgeMatcher(
                    matcher =
                        firstMatcher,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    errorFile =
                        errorFile,
                    printLine = {}
                ).run()

            assertEquals(
                3,
                firstRun.totalRequests
            )

            assertEquals(
                0,
                firstRun.previouslyCompleted
            )

            assertEquals(
                3,
                firstRun.processedThisRun
            )

            assertEquals(
                2,
                firstRun.successfulDecisions
            )

            assertEquals(
                1,
                firstRun.failedThisRun
            )

            assertEquals(
                listOf(
                    "apple yogurt",
                    "banana yogurt",
                    "cherry yogurt"
                ),
                firstMatcher.matchedCatalogKeys
            )

            assertEquals(
                listOf(
                    "apple yogurt",
                    "cherry yogurt"
                ),
                readDecisionCatalogKeys(
                    file = decisionFile
                )
            )

            assertEquals(
                listOf(
                    "banana yogurt"
                ),
                readErrorCatalogKeys(
                    file = errorFile
                )
            )

            val secondMatcher =
                RecordingMatcher(
                    failingCatalogKeys =
                        emptySet()
                )

            val secondRun =
                RunOpenAINutritionKnowledgeMatcher(
                    matcher =
                        secondMatcher,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    errorFile =
                        errorFile,
                    printLine = {}
                ).run()

            assertEquals(
                3,
                secondRun.totalRequests
            )

            assertEquals(
                2,
                secondRun.previouslyCompleted
            )

            assertEquals(
                1,
                secondRun.processedThisRun
            )

            assertEquals(
                3,
                secondRun.successfulDecisions
            )

            assertEquals(
                0,
                secondRun.failedThisRun
            )

            assertEquals(
                listOf(
                    "banana yogurt"
                ),
                secondMatcher.matchedCatalogKeys
            )

            assertEquals(
                listOf(
                    "apple yogurt",
                    "banana yogurt",
                    "cherry yogurt"
                ),
                readDecisionCatalogKeys(
                    file = decisionFile
                )
            )

            assertTrue(
                readErrorCatalogKeys(
                    file = errorFile
                ).isEmpty()
            )

        } finally {
            directory.deleteRecursively()
        }
    }


    private fun writeRequestFixture(
        file: File
    ) {

        file.writeText(
            """
            {
              "version": 1,
              "requests": [
                {
                  "catalogKey": "apple yogurt",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "apple yogurt",
                      "diagnosticScore": 0.95,
                      "sharedTokens": [
                        "apple",
                        "yogurt"
                      ]
                    }
                  ]
                },
                {
                  "catalogKey": "banana yogurt",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "banana yogurt",
                      "diagnosticScore": 0.94,
                      "sharedTokens": [
                        "banana",
                        "yogurt"
                      ]
                    }
                  ]
                },
                {
                  "catalogKey": "cherry yogurt",
                  "serverArtifact": "nutrition.json",
                  "candidates": [
                    {
                      "serverKey": "cherry yogurt",
                      "diagnosticScore": 0.93,
                      "sharedTokens": [
                        "cherry",
                        "yogurt"
                      ]
                    }
                  ]
                }
              ]
            }
            """.trimIndent()
        )
    }


    private fun readDecisionCatalogKeys(
        file: File
    ): List<String> {

        val root =
            JsonParser
                .parseString(
                    file.readText()
                )
                .asJsonObject

        return root["decisions"]
            .asJsonArray
            .map {
                it.asJsonObject["catalogKey"]
                    .asString
            }
    }


    private fun readErrorCatalogKeys(
        file: File
    ): List<String> {

        val root =
            JsonParser
                .parseString(
                    file.readText()
                )
                .asJsonObject

        return root["errors"]
            .asJsonArray
            .map {
                it.asJsonObject["catalogKey"]
                    .asString
            }
    }


    private class RecordingMatcher(
        private val failingCatalogKeys: Set<String>
    ) : CatalogKnowledgeMatcher {

        val matchedCatalogKeys =
            mutableListOf<String>()


        override fun match(
            request: CatalogKnowledgeMatchRequest
        ): CatalogKnowledgeMatchDecision {

            matchedCatalogKeys +=
                request.catalogKey

            if (
                request.catalogKey in
                failingCatalogKeys
            ) {
                error(
                    "Simulated OpenAI failure for " +
                            request.catalogKey
                )
            }

            return CatalogKnowledgeMatchDecision(
                catalogKey =
                    request.catalogKey,
                serverArtifact =
                    request.serverArtifact,
                type =
                    CatalogKnowledgeMatchDecisionType.MATCH,
                selectedServerKey =
                    request.candidates
                        .first()
                        .serverKey,
                confidence =
                    0.95,
                reason =
                    "Recorded test match"
            )
        }
    }
}