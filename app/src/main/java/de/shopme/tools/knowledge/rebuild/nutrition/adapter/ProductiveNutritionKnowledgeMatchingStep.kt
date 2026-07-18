package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionSource
import de.shopme.tools.knowledge.mapping.catalog.runner.RunOpenAINutritionKnowledgeMatcher
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeMatchingStep
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMatchingResult
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode
import java.io.File

class ProductiveNutritionKnowledgeMatchingStep(
    private val runner:
    RunOpenAINutritionKnowledgeMatcher,
    private val decisionFile: File
) : NutritionKnowledgeMatchingStep {

    override fun run(
        mode: NutritionKnowledgeRebuildMode
    ): NutritionKnowledgeRebuildMatchingResult {

        require(
            mode ==
                    NutritionKnowledgeRebuildMode.PRODUCTIVE
        ) {
            "Productive matching step only supports PRODUCTIVE mode."
        }

        /*
         * Der produktive Lauf setzt auf dem aktuellen Decision-
         * Checkpoint auf.
         *
         * Die ungelösten alten Decisions wurden bereits durch den
         * OFFLINE-Rebuild entfernt. Während eines produktiven Laufs
         * persistiert der Runner neue Decisions fortlaufend.
         *
         * Diese Decisions dürfen bei einer Wiederaufnahme nicht
         * erneut durch den Validation-Report-Preparer entfernt werden.
         */
        val beforeSourceCounts =
            readDecisionSourceCounts(
                file =
                    decisionFile
            )

        val result =
            runner.run()

        val afterSourceCounts =
            readDecisionSourceCounts(
                file =
                    decisionFile
            )

        require(
            afterSourceCounts.totalCount ==
                    result.totalRequests
        ) {
            "Productive nutrition decision batch is incomplete: " +
                    "decisions=${afterSourceCounts.totalCount}, " +
                    "requests=${result.totalRequests}."
        }

        require(
            result.previouslyCompleted +
                    result.processedThisRun ==
                    result.totalRequests
        ) {
            "Previously completed and newly processed decisions " +
                    "do not cover all nutrition requests: " +
                    "previous=${result.previouslyCompleted}, " +
                    "processed=${result.processedThisRun}, " +
                    "requests=${result.totalRequests}."
        }

        val newLocalModelDecisionCount =
            afterSourceCounts.localModelCount -
                    beforeSourceCounts.localModelCount

        val newChatGptDecisionCount =
            afterSourceCounts.chatGptCount -
                    beforeSourceCounts.chatGptCount

        require(newLocalModelDecisionCount >= 0) {
            "LOCAL_MODEL decision count decreased during " +
                    "productive matching."
        }

        require(newChatGptDecisionCount >= 0) {
            "CHAT_GPT decision count decreased during productive " +
                    "matching."
        }

        val successfulThisRun =
            result.processedThisRun -
                    result.failedThisRun

        require(successfulThisRun >= 0) {
            "Successful decision count for this run is negative: " +
                    "processed=${result.processedThisRun}, " +
                    "failed=${result.failedThisRun}."
        }

        require(
            newLocalModelDecisionCount +
                    newChatGptDecisionCount ==
                    successfulThisRun
        ) {
            "New decision source counts differ from successful " +
                    "decisions of this run: " +
                    "local=$newLocalModelDecisionCount, " +
                    "chatGpt=$newChatGptDecisionCount, " +
                    "successfulThisRun=$successfulThisRun, " +
                    "processed=${result.processedThisRun}, " +
                    "failed=${result.failedThisRun}."
        }

        require(
            successfulThisRun >= 0
        ) {
            "Successful decision count for this run is negative: " +
                    "processed=${result.processedThisRun}, " +
                    "failed=${result.failedThisRun}."
        }

        require(
            newLocalModelDecisionCount +
                    newChatGptDecisionCount ==
                    successfulThisRun
        ) {
            "New decision source counts differ from successful " +
                    "decisions of this run: " +
                    "local=$newLocalModelDecisionCount, " +
                    "chatGpt=$newChatGptDecisionCount, " +
                    "successfulThisRun=$successfulThisRun, " +
                    "processed=${result.processedThisRun}, " +
                    "failed=${result.failedThisRun}."
        }

        require(
            successfulThisRun +
                    result.failedThisRun ==
                    result.processedThisRun
        ) {
            "Productive matching outcomes do not cover all newly " +
                    "processed requests: " +
                    "successfulThisRun=$successfulThisRun, " +
                    "failed=${result.failedThisRun}, " +
                    "processed=${result.processedThisRun}."
        }

        return NutritionKnowledgeRebuildMatchingResult(
            requestCount =
                result.totalRequests,
            previouslyCompletedCount =
                result.previouslyCompleted,
            processedCount =
                result.processedThisRun,
            localModelDecisionCount =
                newLocalModelDecisionCount,
            chatGptDecisionCount =
                newChatGptDecisionCount,
            gptFallbackRequiredCount =
                0,
            matchCount =
                result.matchCount,
            noMatchCount =
                result.noMatchCount,
            errorCount =
                result.failedThisRun
        )
    }

    private fun readDecisionSourceCounts(
        file: File
    ): DecisionSourceCounts {

        if (!file.isFile) {

            return DecisionSourceCounts(
                totalCount =
                    0,
                localModelCount =
                    0,
                chatGptCount =
                    0
            )
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Nutrition decision file must contain a JSON object: " +
                    file.absolutePath
        }

        val decisions =
            root.asJsonObject["decisions"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: error(
                    "Nutrition decision file contains no " +
                            "'decisions' array: " +
                            file.absolutePath
                )

        val parsedDecisions =
            decisions.map { element ->

                require(element.isJsonObject) {
                    "Nutrition decision entry must be a JSON object."
                }

                readDecision(
                    objectValue =
                        element.asJsonObject
                )
            }

        val duplicateIdentities =
            parsedDecisions
                .groupingBy {
                    DecisionIdentity(
                        catalogKey =
                            it.catalogKey,
                        serverArtifact =
                            it.serverArtifact
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateIdentities.isEmpty()) {
            "Nutrition decision file contains duplicate " +
                    "identities: " +
                    duplicateIdentities
                        .sortedWith(
                            compareBy<DecisionIdentity>(
                                { it.serverArtifact },
                                { it.catalogKey }
                            )
                        )
                        .take(MAX_DIAGNOSTIC_IDENTITIES)
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverArtifact}"
                        }
        }

        return DecisionSourceCounts(
            totalCount =
                parsedDecisions.size,
            localModelCount =
                parsedDecisions.count {
                    it.decisionSource ==
                            CatalogKnowledgeMatchDecisionSource
                                .LOCAL_MODEL
                },
            chatGptCount =
                parsedDecisions.count {
                    it.decisionSource ==
                            CatalogKnowledgeMatchDecisionSource
                                .CHAT_GPT
                }
        )
    }

    private fun readDecision(
        objectValue: JsonObject
    ): CatalogKnowledgeMatchDecision {

        /*
         * Gson setzt Kotlin-Defaultwerte beim Deserialisieren nicht
         * zuverlässig ein. Deshalb wird decisionSource bei älteren
         * Decisions explizit auf CHAT_GPT gesetzt.
         */
        val source =
            objectValue["decisionSource"]
                ?.takeIf {
                    !it.isJsonNull &&
                            it.isJsonPrimitive &&
                            it.asJsonPrimitive.isString
                }
                ?.asString
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let(
                    CatalogKnowledgeMatchDecisionSource::valueOf
                )
                ?: CatalogKnowledgeMatchDecisionSource.CHAT_GPT

        val decision =
            GSON.fromJson(
                objectValue,
                CatalogKnowledgeMatchDecision::class.java
            )

        return decision.copy(
            decisionSource =
                source
        )
    }

    private data class DecisionSourceCounts(
        val totalCount: Int,
        val localModelCount: Int,
        val chatGptCount: Int
    )

    private data class DecisionIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )

    private companion object {

        const val MAX_DIAGNOSTIC_IDENTITIES =
            10

        val GSON: Gson =
            GsonBuilder()
                .disableHtmlEscaping()
                .create()
    }
}