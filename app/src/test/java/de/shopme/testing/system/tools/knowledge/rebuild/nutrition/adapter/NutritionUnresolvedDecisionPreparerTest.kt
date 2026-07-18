package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.NutritionUnresolvedDecisionPreparer
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class NutritionUnresolvedDecisionPreparerTest {

    @Test
    fun retainOnlyAcceptedNutritionDecisions() {

        val directory =
            createTempDirectory(
                prefix =
                    "nutrition-unresolved-decisions-"
            )
                .toFile()

        try {
            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "decisions": [
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "type": "MATCH",
                                  "selectedServerKey": "apple raw",
                                  "confidence": 0.95,
                                  "reason": "Accepted.",
                                  "decisionSource": "CHAT_GPT"
                                },
                                {
                                  "catalogKey": "fruit yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "type": "MATCH",
                                  "selectedServerKey": "cherry yogurt",
                                  "confidence": 0.72,
                                  "reason": "Low confidence.",
                                  "decisionSource": "CHAT_GPT"
                                },
                                {
                                  "catalogKey": "unknown meal",
                                  "serverArtifact": "nutrition.json",
                                  "type": "NO_MATCH",
                                  "selectedServerKey": null,
                                  "confidence": 0.90,
                                  "reason": "No match.",
                                  "decisionSource": "CHAT_GPT"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val validationReportFile =
                File(
                    directory,
                    "nutrition.mapping-validation-report.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "diagnostics": [
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "validationStatus": "ACCEPTED"
                                },
                                {
                                  "catalogKey": "fruit yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "validationStatus": "REJECTED_LOW_CONFIDENCE"
                                },
                                {
                                  "catalogKey": "unknown meal",
                                  "serverArtifact": "nutrition.json",
                                  "validationStatus": "REJECTED_NO_MATCH"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val result =
                NutritionUnresolvedDecisionPreparer(
                    decisionFile =
                        decisionFile,
                    validationReportFile =
                        validationReportFile
                )
                    .prepare()

            assertEquals(
                expected = 3,
                actual =
                    result.existingDecisionCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.acceptedDecisionCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.acceptedLocalModelDecisionCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.acceptedChatGptDecisionCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.unresolvedDecisionCount
            )

            val persisted =
                JsonParser.parseString(
                    decisionFile.readText()
                )
                    .asJsonObject["decisions"]
                    .asJsonArray

            assertEquals(
                expected = 1,
                actual =
                    persisted.size()
            )

            assertEquals(
                expected =
                    "apple",
                actual =
                    persisted[0]
                        .asJsonObject["catalogKey"]
                        .asString
            )

        } finally {
            directory.deleteRecursively()
        }
    }
}