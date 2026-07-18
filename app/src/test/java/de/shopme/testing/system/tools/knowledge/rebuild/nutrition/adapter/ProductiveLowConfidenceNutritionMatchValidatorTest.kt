package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.ProductiveLowConfidenceNutritionMatchValidator
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProductiveLowConfidenceNutritionMatchValidatorTest {

    @Test
    fun validateOnlyNewLowConfidenceMatchDecisions() {

        val directory =
            Files.createTempDirectory(
                "productive-low-confidence-nutrition"
            )
                .toFile()

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

            val validationFile =
                File(
                    directory,
                    "nutrition.low-confidence-validation.json"
                )

            requestFile.writeText(
                """
                {
                  "version": 1,
                  "requests": [
                    {
                      "catalogKey": "canned pumpkin pieces",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "canned pumpkin",
                          "diagnosticScore": 0.91,
                          "sharedTokens": [
                            "canned",
                            "pumpkin"
                          ]
                        }
                      ]
                    },
                    {
                      "catalogKey": "fresh trout",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "steelhead trout fresh",
                          "diagnosticScore": 0.88,
                          "sharedTokens": [
                            "fresh",
                            "trout"
                          ]
                        }
                      ]
                    },
                    {
                      "catalogKey": "frozen cheese pretzel",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "cheddar cheese pretzel",
                          "diagnosticScore": 0.84,
                          "sharedTokens": [
                            "cheese",
                            "pretzel"
                          ]
                        }
                      ]
                    },
                    {
                      "catalogKey": "high confidence apple",
                      "serverArtifact": "nutrition.json",
                      "candidates": [
                        {
                          "serverKey": "apple",
                          "diagnosticScore": 0.95,
                          "sharedTokens": [
                            "apple"
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
                      "catalogKey": "canned pumpkin pieces",
                      "serverArtifact": "nutrition.json",
                      "type": "MATCH",
                      "selectedServerKey": "canned pumpkin",
                      "confidence": 0.78,
                      "reason": "Existing representative decision.",
                      "decisionSource": "CHAT_GPT"
                    },
                    {
                      "catalogKey": "fresh trout",
                      "serverArtifact": "nutrition.json",
                      "type": "MATCH",
                      "selectedServerKey": "steelhead trout fresh",
                      "confidence": 0.78,
                      "reason": "Fresh trout representative match.",
                      "decisionSource": "CHAT_GPT"
                    },
                    {
                      "catalogKey": "frozen cheese pretzel",
                      "serverArtifact": "nutrition.json",
                      "type": "MATCH",
                      "selectedServerKey": "cheddar cheese pretzel",
                      "confidence": 0.72,
                      "reason": "Pretzel candidate.",
                      "decisionSource": "CHAT_GPT"
                    },
                    {
                      "catalogKey": "high confidence apple",
                      "serverArtifact": "nutrition.json",
                      "type": "MATCH",
                      "selectedServerKey": "apple",
                      "confidence": 0.91,
                      "reason": "High-confidence match.",
                      "decisionSource": "LOCAL_MODEL"
                    }
                  ]
                }
                """.trimIndent()
            )

            validationFile.writeText(
                """
                {
                  "version": 1,
                  "entries": [
                    {
                      "catalogKey": "canned pumpkin pieces",
                      "selectedServerKey": "canned pumpkin",
                      "candidateRank": 1,
                      "originalConfidence": 0.78,
                      "originalDecisionReason": "Existing representative decision.",
                      "originalValidationStatus": "REJECTED_LOW_CONFIDENCE",
                      "originalValidationReason": "Decision confidence 0.78 is below minimum 0.8",
                      "decisionType": "REPRESENTATIVE",
                      "reasons": [
                        "COMPATIBLE_SPECIALIZATION",
                        "SAME_PRODUCT_CLASS"
                      ],
                      "accepted": true
                    }
                  ]
                }
                """.trimIndent()
            )

            val result =
                ProductiveLowConfidenceNutritionMatchValidator(
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    representativeValidationFile =
                        validationFile,
                    minimumConfidence =
                        0.80
                )
                    .run()

            assertEquals(
                expected =
                    3,
                actual =
                    result.lowConfidenceMatchCount
            )

            assertEquals(
                expected =
                    1,
                actual =
                    result.existingValidationCount
            )

            assertEquals(
                expected =
                    2,
                actual =
                    result.newlyValidatedCount
            )

            assertEquals(
                expected =
                    2,
                actual =
                    result.finalValidationEntryCount -
                            1
            )

            val root =
                JsonParser.parseString(
                    validationFile.readText()
                )
                    .asJsonObject

            val entries =
                root["entries"]
                    .asJsonArray

            assertEquals(
                expected =
                    3,
                actual =
                    entries.size()
            )

            val entriesByCatalogKey =
                entries.associateBy {
                    it.asJsonObject["catalogKey"]
                        .asString
                }

            assertTrue(
                "fresh trout" in
                        entriesByCatalogKey
            )

            assertTrue(
                "frozen cheese pretzel" in
                        entriesByCatalogKey
            )

            assertFalse(
                "high confidence apple" in
                        entriesByCatalogKey
            )

            val frozenPretzel =
                requireNotNull(
                    entriesByCatalogKey[
                        "frozen cheese pretzel"
                    ]
                )
                    .asJsonObject

            assertEquals(
                expected =
                    "REPRESENTATIVE",
                actual =
                    frozenPretzel[
                        "decisionType"
                    ]
                        .asString
            )

            assertTrue(
                actual =
                    frozenPretzel[
                        "accepted"
                    ]
                        .asBoolean
            )

        } finally {

            directory.deleteRecursively()
        }
    }
}