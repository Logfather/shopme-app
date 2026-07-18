package de.shopme.testing.system.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionSource
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatcher
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.GptFallbackRequiredException
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.NutritionUnresolvedDecisionPreparer
import de.shopme.tools.knowledge.rebuild.nutrition.adapter.OfflineNutritionKnowledgeMatchingStep
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfflineNutritionKnowledgeMatchingStepTest {

    @Test
    fun persistLocalDecisionsAndReportGptFallbacks() {

        val directory =
            createTempDirectory(
                prefix =
                    "offline-nutrition-matching-"
            )
                .toFile()

        try {
            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "requests": [
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "apple raw",
                                      "diagnosticScore": 0.99,
                                      "sharedTokens": [
                                        "apple"
                                      ]
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "fruit yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "cherry fruit yogurt",
                                      "diagnosticScore": 0.72,
                                      "sharedTokens": [
                                        "fruit",
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

            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )

            val validationReportFile =
                createValidationReport(
                    directory =
                        directory,
                    diagnostics =
                        emptyList()
                )

            var appleCalls =
                0

            var yogurtCalls =
                0

            val matcher =
                object : CatalogKnowledgeMatcher {

                    override fun match(
                        request:
                        CatalogKnowledgeMatchRequest
                    ): CatalogKnowledgeMatchDecision {

                        return when (
                            request.catalogKey
                        ) {

                            "apple" -> {

                                appleCalls++

                                CatalogKnowledgeMatchDecision(
                                    catalogKey =
                                        request.catalogKey,
                                    serverArtifact =
                                        request.serverArtifact,
                                    type =
                                        CatalogKnowledgeMatchDecisionType
                                            .MATCH,
                                    selectedServerKey =
                                        "apple raw",
                                    confidence =
                                        0.991,
                                    reason =
                                        "Accepted by local model.",
                                    decisionSource =
                                        CatalogKnowledgeMatchDecisionSource
                                            .LOCAL_MODEL
                                )
                            }

                            "fruit yogurt" -> {

                                yogurtCalls++

                                throw GptFallbackRequiredException(
                                    catalogKey =
                                        request.catalogKey
                                )
                            }

                            else -> {
                                error(
                                    "Unexpected request: " +
                                            request.catalogKey
                                )
                            }
                        }
                    }
                }

            val result =
                OfflineNutritionKnowledgeMatchingStep(
                    matcher =
                        matcher,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    unresolvedDecisionPreparer =
                        NutritionUnresolvedDecisionPreparer(
                            decisionFile =
                                decisionFile,
                            validationReportFile =
                                validationReportFile
                        )
                )
                    .run(
                        mode =
                            NutritionKnowledgeRebuildMode.OFFLINE
                    )

            assertEquals(
                expected = 2,
                actual =
                    result.requestCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.previouslyCompletedCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.processedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.localModelDecisionCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.chatGptDecisionCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.gptFallbackRequiredCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.matchCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.noMatchCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.errorCount
            )

            assertEquals(
                expected = 1,
                actual =
                    appleCalls
            )

            assertEquals(
                expected = 1,
                actual =
                    yogurtCalls
            )

            assertTrue(
                decisionFile.isFile
            )

            val persisted =
                JsonParser.parseString(
                    decisionFile.readText()
                )
                    .asJsonObject

            val decisions =
                persisted["decisions"]
                    .asJsonArray

            assertEquals(
                expected = 1,
                actual =
                    decisions.size()
            )

            val decision =
                decisions.single()
                    .asJsonObject

            assertEquals(
                expected =
                    "apple",
                actual =
                    decision["catalogKey"]
                        .asString
            )

            assertEquals(
                expected =
                    "MATCH",
                actual =
                    decision["type"]
                        .asString
            )

            assertEquals(
                expected =
                    "LOCAL_MODEL",
                actual =
                    decision["decisionSource"]
                        .asString
            )

            assertFalse(
                decisions.any {
                    it.asJsonObject["catalogKey"]
                        .asString ==
                            "fruit yogurt"
                }
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun preserveAcceptedDecisionsAndOnlyProcessUnresolvedRequests() {

        val directory =
            createTempDirectory(
                prefix =
                    "offline-nutrition-resume-"
            )
                .toFile()

        try {
            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "requests": [
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "apple raw",
                                      "diagnosticScore": 0.99,
                                      "sharedTokens": [
                                        "apple"
                                      ]
                                    }
                                  ]
                                },
                                {
                                  "catalogKey": "banana",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "banana raw",
                                      "diagnosticScore": 0.98,
                                      "sharedTokens": [
                                        "banana"
                                      ]
                                    }
                                  ]
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

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
                                  "confidence": 0.99,
                                  "reason": "Existing local decision.",
                                  "decisionSource": "LOCAL_MODEL"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val validationReportFile =
                createValidationReport(
                    directory =
                        directory,
                    diagnostics =
                        listOf(
                            ValidationDiagnosticFixture(
                                catalogKey =
                                    "apple",
                                validationStatus =
                                    "ACCEPTED"
                            )
                        )
                )

            var matcherCallCount =
                0

            val matcher =
                object : CatalogKnowledgeMatcher {

                    override fun match(
                        request:
                        CatalogKnowledgeMatchRequest
                    ): CatalogKnowledgeMatchDecision {

                        matcherCallCount++

                        assertEquals(
                            expected =
                                "banana",
                            actual =
                                request.catalogKey
                        )

                        return CatalogKnowledgeMatchDecision(
                            catalogKey =
                                request.catalogKey,
                            serverArtifact =
                                request.serverArtifact,
                            type =
                                CatalogKnowledgeMatchDecisionType
                                    .MATCH,
                            selectedServerKey =
                                "banana raw",
                            confidence =
                                0.992,
                            reason =
                                "Accepted by local model.",
                            decisionSource =
                                CatalogKnowledgeMatchDecisionSource
                                    .LOCAL_MODEL
                        )
                    }
                }

            val result =
                OfflineNutritionKnowledgeMatchingStep(
                    matcher =
                        matcher,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    unresolvedDecisionPreparer =
                        NutritionUnresolvedDecisionPreparer(
                            decisionFile =
                                decisionFile,
                            validationReportFile =
                                validationReportFile
                        )
                )
                    .run(
                        mode =
                            NutritionKnowledgeRebuildMode.OFFLINE
                    )

            assertEquals(
                expected = 2,
                actual =
                    result.requestCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.previouslyCompletedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.processedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.localModelDecisionCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.chatGptDecisionCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.gptFallbackRequiredCount
            )

            assertEquals(
                expected = 2,
                actual =
                    result.matchCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.noMatchCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.errorCount
            )

            assertEquals(
                expected = 1,
                actual =
                    matcherCallCount
            )

            val decisions =
                JsonParser.parseString(
                    decisionFile.readText()
                )
                    .asJsonObject["decisions"]
                    .asJsonArray

            assertEquals(
                expected = 2,
                actual =
                    decisions.size()
            )

            assertEquals(
                expected =
                    listOf(
                        "apple",
                        "banana"
                    ),
                actual =
                    decisions.map {
                        it.asJsonObject["catalogKey"]
                            .asString
                    }
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun removeRejectedDecisionAndProcessRequestAgain() {

        val directory =
            createTempDirectory(
                prefix =
                    "offline-nutrition-reprocess-"
            )
                .toFile()

        try {
            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "requests": [
                                {
                                  "catalogKey": "fruit yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "cherry fruit yogurt",
                                      "diagnosticScore": 0.99,
                                      "sharedTokens": [
                                        "fruit",
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
                                  "catalogKey": "fruit yogurt",
                                  "serverArtifact": "nutrition.json",
                                  "type": "MATCH",
                                  "selectedServerKey": "cherry fruit yogurt",
                                  "confidence": 0.72,
                                  "reason": "Existing low-confidence decision.",
                                  "decisionSource": "CHAT_GPT"
                                }
                              ]
                            }
                            """.trimIndent()
                        )
                    }

            val validationReportFile =
                createValidationReport(
                    directory =
                        directory,
                    diagnostics =
                        listOf(
                            ValidationDiagnosticFixture(
                                catalogKey =
                                    "fruit yogurt",
                                validationStatus =
                                    "REJECTED_LOW_CONFIDENCE"
                            )
                        )
                )

            var matcherCallCount =
                0

            val matcher =
                object : CatalogKnowledgeMatcher {

                    override fun match(
                        request:
                        CatalogKnowledgeMatchRequest
                    ): CatalogKnowledgeMatchDecision {

                        matcherCallCount++

                        return CatalogKnowledgeMatchDecision(
                            catalogKey =
                                request.catalogKey,
                            serverArtifact =
                                request.serverArtifact,
                            type =
                                CatalogKnowledgeMatchDecisionType
                                    .MATCH,
                            selectedServerKey =
                                "cherry fruit yogurt",
                            confidence =
                                0.991,
                            reason =
                                "Accepted by local model.",
                            decisionSource =
                                CatalogKnowledgeMatchDecisionSource
                                    .LOCAL_MODEL
                        )
                    }
                }

            val result =
                OfflineNutritionKnowledgeMatchingStep(
                    matcher =
                        matcher,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    unresolvedDecisionPreparer =
                        NutritionUnresolvedDecisionPreparer(
                            decisionFile =
                                decisionFile,
                            validationReportFile =
                                validationReportFile
                        )
                )
                    .run(
                        mode =
                            NutritionKnowledgeRebuildMode.OFFLINE
                    )

            assertEquals(
                expected = 0,
                actual =
                    result.previouslyCompletedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.processedCount
            )

            assertEquals(
                expected = 1,
                actual =
                    result.localModelDecisionCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.gptFallbackRequiredCount
            )

            assertEquals(
                expected = 1,
                actual =
                    matcherCallCount
            )

            val decisions =
                JsonParser.parseString(
                    decisionFile.readText()
                )
                    .asJsonObject["decisions"]
                    .asJsonArray

            assertEquals(
                expected = 1,
                actual =
                    decisions.size()
            )

            val persistedDecision =
                decisions.single()
                    .asJsonObject

            assertEquals(
                expected =
                    "LOCAL_MODEL",
                actual =
                    persistedDecision["decisionSource"]
                        .asString
            )

            assertTrue(
                persistedDecision["confidence"]
                    .asDouble >=
                        0.98
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    @Test
    fun rejectNonLocalDecisionInOfflineMode() {

        val directory =
            createTempDirectory(
                prefix =
                    "offline-reject-chatgpt-"
            )
                .toFile()

        try {
            val requestFile =
                File(
                    directory,
                    "nutrition.match-requests.json"
                )
                    .apply {
                        writeText(
                            """
                            {
                              "version": 1,
                              "requests": [
                                {
                                  "catalogKey": "apple",
                                  "serverArtifact": "nutrition.json",
                                  "candidates": [
                                    {
                                      "serverKey": "apple raw",
                                      "diagnosticScore": 0.99,
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
                    }

            val decisionFile =
                File(
                    directory,
                    "nutrition.match-decisions.json"
                )

            val validationReportFile =
                createValidationReport(
                    directory =
                        directory,
                    diagnostics =
                        emptyList()
                )

            val matcher =
                object : CatalogKnowledgeMatcher {

                    override fun match(
                        request:
                        CatalogKnowledgeMatchRequest
                    ): CatalogKnowledgeMatchDecision {

                        return CatalogKnowledgeMatchDecision(
                            catalogKey =
                                request.catalogKey,
                            serverArtifact =
                                request.serverArtifact,
                            type =
                                CatalogKnowledgeMatchDecisionType
                                    .MATCH,
                            selectedServerKey =
                                "apple raw",
                            confidence =
                                0.99,
                            reason =
                                "Unexpected ChatGPT decision.",
                            decisionSource =
                                CatalogKnowledgeMatchDecisionSource
                                    .CHAT_GPT
                        )
                    }
                }

            val result =
                OfflineNutritionKnowledgeMatchingStep(
                    matcher =
                        matcher,
                    requestFile =
                        requestFile,
                    decisionFile =
                        decisionFile,
                    unresolvedDecisionPreparer =
                        NutritionUnresolvedDecisionPreparer(
                            decisionFile =
                                decisionFile,
                            validationReportFile =
                                validationReportFile
                        )
                )
                    .run(
                        mode =
                            NutritionKnowledgeRebuildMode.OFFLINE
                    )

            assertEquals(
                expected = 1,
                actual =
                    result.errorCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.localModelDecisionCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.chatGptDecisionCount
            )

            assertEquals(
                expected = 0,
                actual =
                    result.gptFallbackRequiredCount
            )

            assertTrue(
                decisionFile.isFile
            )

            val decisions =
                JsonParser.parseString(
                    decisionFile.readText()
                )
                    .asJsonObject["decisions"]
                    .asJsonArray

            assertTrue(
                decisions.isEmpty()
            )

        } finally {
            directory.deleteRecursively()
        }
    }

    private fun createValidationReport(
        directory: File,
        diagnostics:
        List<ValidationDiagnosticFixture>
    ): File {

        val diagnosticsJson =
            diagnostics.joinToString(
                separator = ",\n"
            ) { diagnostic ->

                """
                {
                  "catalogKey": "${diagnostic.catalogKey}",
                  "serverArtifact": "nutrition.json",
                  "validationStatus": "${diagnostic.validationStatus}"
                }
                """.trimIndent()
            }

        return File(
            directory,
            "nutrition.mapping-validation-report.json"
        )
            .apply {
                writeText(
                    """
                    {
                      "version": 1,
                      "diagnostics": [
                        $diagnosticsJson
                      ]
                    }
                    """.trimIndent()
                )
            }
    }

    private data class ValidationDiagnosticFixture(
        val catalogKey: String,
        val validationStatus: String
    )
}