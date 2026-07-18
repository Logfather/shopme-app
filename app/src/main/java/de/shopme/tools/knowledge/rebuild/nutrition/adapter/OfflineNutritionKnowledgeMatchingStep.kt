package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchCandidate
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionSource
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisions
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequest
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchRequestContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatcher
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeMatchingStep
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMatchingResult
import de.shopme.tools.knowledge.rebuild.nutrition.NutritionKnowledgeRebuildMode
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class OfflineNutritionKnowledgeMatchingStep(
    private val matcher: CatalogKnowledgeMatcher,
    private val requestFile: File,
    private val decisionFile: File,
    private val unresolvedDecisionPreparer:
    NutritionUnresolvedDecisionPreparer
) : NutritionKnowledgeMatchingStep {

    override fun run(
        mode: NutritionKnowledgeRebuildMode
    ): NutritionKnowledgeRebuildMatchingResult {

        require(
            mode ==
                    NutritionKnowledgeRebuildMode.OFFLINE
        ) {
            "Offline nutrition matching step only supports " +
                    "OFFLINE mode."
        }

        /*
         * Wichtig:
         *
         * Die Decision-Datei muss zuerst bereinigt werden.
         * Erst danach darf der verbliebene ACCEPTED-Bestand
         * eingelesen werden.
         */
        val preparation =
            unresolvedDecisionPreparer.prepare()

        val requests =
            readRequests(
                file = requestFile
            )

        val retainedAcceptedDecisions =
            readDecisions(
                file = decisionFile
            )

        /*
         * Dieser Vergleich ist nur an genau dieser Stelle korrekt:
         * direkt nach prepare(), bevor neue lokale Decisions
         * hinzugefügt werden.
         */
        require(
            preparation.retainedDecisionCount ==
                    retainedAcceptedDecisions.size
        ) {
            "Retained accepted decision count differs from the " +
                    "prepared decision file: " +
                    "preparation=${preparation.retainedDecisionCount}, " +
                    "file=${retainedAcceptedDecisions.size}"
        }

        require(
            preparation.acceptedDecisionCount ==
                    retainedAcceptedDecisions.size
        ) {
            "Accepted decision count differs from the prepared " +
                    "decision file: " +
                    "accepted=${preparation.acceptedDecisionCount}, " +
                    "file=${retainedAcceptedDecisions.size}"
        }

        require(
            retainedAcceptedDecisions.all {
                it.serverArtifact ==
                        NUTRITION_ARTIFACT
            }
        ) {
            "Prepared nutrition decision file contains a decision " +
                    "for another server artifact."
        }

        val existingByIdentity =
            retainedAcceptedDecisions
                .associateBy {
                    it.identity()
                }
                .toMutableMap()

        val duplicateRequestIdentities =
            requests
                .groupingBy {
                    it.identity()
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(
            duplicateRequestIdentities.isEmpty()
        ) {
            "Duplicate nutrition match requests: " +
                    duplicateRequestIdentities
                        .sortedWith(
                            compareBy<MatchIdentity>(
                                { it.serverArtifact },
                                { it.catalogKey }
                            )
                        )
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverArtifact}"
                        }
        }

        val requestsToProcess =
            requests
                .filterNot { request ->
                    request.identity() in
                            existingByIdentity
                }

        require(
            preparation.acceptedDecisionCount +
                    requestsToProcess.size ==
                    requests.size
        ) {
            "Accepted and unresolved nutrition requests do not " +
                    "cover the complete request batch: " +
                    "accepted=${preparation.acceptedDecisionCount}, " +
                    "unresolved=${requestsToProcess.size}, " +
                    "requests=${requests.size}"
        }

        /*
         * existingDecisionCount kann kleiner als requestCount sein,
         * wenn vorher noch nicht für alle Requests eine Decision
         * erzeugt wurde.
         *
         * Daher vergleichen wir unresolvedDecisionCount nicht direkt
         * mit requestsToProcess.size.
         */
        require(
            preparation.unresolvedDecisionCount <=
                    requestsToProcess.size
        ) {
            "Prepared unresolved decision count exceeds unresolved " +
                    "request count: " +
                    "decisions=${preparation.unresolvedDecisionCount}, " +
                    "requests=${requestsToProcess.size}"
        }

        var localModelDecisionCount =
            0

        var gptFallbackRequiredCount =
            0

        var errorCount =
            0

        requestsToProcess.forEach { request ->

            try {
                val decision =
                    matcher.match(
                        request = request
                    )

                validateLocalDecision(
                    request = request,
                    decision = decision
                )

                existingByIdentity[
                    request.identity()
                ] = decision

                localModelDecisionCount++

            } catch (
                exception: GptFallbackRequiredException
            ) {
                require(
                    exception.catalogKey ==
                            request.catalogKey
                ) {
                    "GPT fallback exception catalogKey differs " +
                            "from the current request: " +
                            "${exception.catalogKey} != " +
                            request.catalogKey
                }

                /*
                 * Kein Fehler und keine finale Decision.
                 *
                 * Der Request bleibt für einen späteren
                 * PRODUCTIVE-Lauf offen.
                 */
                gptFallbackRequiredCount++

            } catch (
                exception: Exception
            ) {
                errorCount++
            }
        }

        val finalDecisions =
            existingByIdentity
                .values
                .sortedWith(
                    CatalogKnowledgeMatchDecisions.DECISION_ORDER
                )

        persistDecisions(
            decisions =
                finalDecisions
        )

        require(
            localModelDecisionCount +
                    gptFallbackRequiredCount +
                    errorCount ==
                    requestsToProcess.size
        ) {
            "Offline matching outcomes do not cover every " +
                    "processed request: " +
                    "local=$localModelDecisionCount, " +
                    "fallback=$gptFallbackRequiredCount, " +
                    "errors=$errorCount, " +
                    "processed=${requestsToProcess.size}"
        }

        require(
            finalDecisions.size ==
                    retainedAcceptedDecisions.size +
                    localModelDecisionCount
        ) {
            "Final decision count is inconsistent: " +
                    "retained=${retainedAcceptedDecisions.size}, " +
                    "local=$localModelDecisionCount, " +
                    "final=${finalDecisions.size}"
        }

        return NutritionKnowledgeRebuildMatchingResult(
            requestCount =
                requests.size,
            previouslyCompletedCount =
                retainedAcceptedDecisions.size,
            processedCount =
                requestsToProcess.size,
            localModelDecisionCount =
                localModelDecisionCount,
            chatGptDecisionCount =
                0,
            gptFallbackRequiredCount =
                gptFallbackRequiredCount,
            matchCount =
                finalDecisions.count {
                    it.type ==
                            CatalogKnowledgeMatchDecisionType.MATCH
                },
            noMatchCount =
                finalDecisions.count {
                    it.type ==
                            CatalogKnowledgeMatchDecisionType.NO_MATCH
                },
            errorCount =
                errorCount
        )
    }

    private fun validateLocalDecision(
        request: CatalogKnowledgeMatchRequest,
        decision: CatalogKnowledgeMatchDecision
    ) {
        require(
            decision.catalogKey ==
                    request.catalogKey
        ) {
            "Local decision catalogKey differs from request: " +
                    "${decision.catalogKey} != ${request.catalogKey}"
        }

        require(
            decision.serverArtifact ==
                    request.serverArtifact
        ) {
            "Local decision serverArtifact differs from request: " +
                    "${decision.serverArtifact} != " +
                    request.serverArtifact
        }

        require(
            decision.decisionSource ==
                    CatalogKnowledgeMatchDecisionSource.LOCAL_MODEL
        ) {
            "Offline matching produced a non-local decision for " +
                    "catalog key '${request.catalogKey}': " +
                    decision.decisionSource
        }

        require(
            decision.type ==
                    CatalogKnowledgeMatchDecisionType.MATCH
        ) {
            "Offline local matcher may only persist confident " +
                    "MATCH decisions. Found ${decision.type} for " +
                    "'${request.catalogKey}'."
        }

        val selectedServerKey =
            requireNotNull(
                decision.selectedServerKey
            ) {
                "Local MATCH decision has no selectedServerKey: " +
                        request.catalogKey
            }

        require(
            request.candidates.any {
                it.serverKey ==
                        selectedServerKey
            }
        ) {
            "Local matcher selected a server key that was not " +
                    "provided as candidate: " +
                    "${request.catalogKey} -> $selectedServerKey"
        }
    }

    private fun readRequests(
        file: File
    ): List<CatalogKnowledgeMatchRequest> {

        require(file.isFile) {
            "Nutrition request file does not exist: " +
                    file.absolutePath
        }

        val root =
            parseJsonObject(
                file = file
            )

        val version =
            root.requiredInt(
                key = "version"
            )

        require(
            version ==
                    CatalogKnowledgeMatchRequestContract
                        .CURRENT_VERSION
        ) {
            "Unsupported nutrition match request version: " +
                    version
        }

        return root
            .requiredArray(
                key = "requests"
            )
            .map { requestElement ->

                val requestObject =
                    requestElement.asJsonObject

                val candidates =
                    requestObject
                        .requiredArray(
                            key = "candidates"
                        )
                        .map { candidateElement ->

                            val candidateObject =
                                candidateElement.asJsonObject

                            CatalogKnowledgeMatchCandidate(
                                serverKey =
                                    candidateObject.requiredString(
                                        key = "serverKey"
                                    ),
                                diagnosticScore =
                                    candidateObject.requiredDouble(
                                        key = "diagnosticScore"
                                    ),
                                sharedTokens =
                                    candidateObject
                                        .requiredArray(
                                            key = "sharedTokens"
                                        )
                                        .map { tokenElement ->

                                            require(
                                                tokenElement
                                                    .isJsonPrimitive &&
                                                        tokenElement
                                                            .asJsonPrimitive
                                                            .isString
                                            ) {
                                                "Expected shared token string."
                                            }

                                            tokenElement
                                                .asString
                                                .trim()
                                        }
                                        .filter {
                                            it.isNotBlank()
                                        }
                                        .distinct()
                                        .sorted()
                            )
                        }
                        .sortedWith(
                            CatalogKnowledgeMatchRequest
                                .CANDIDATE_ORDER
                        )

                CatalogKnowledgeMatchRequest(
                    catalogKey =
                        requestObject.requiredString(
                            key = "catalogKey"
                        ),
                    serverArtifact =
                        requestObject.requiredString(
                            key = "serverArtifact"
                        ),
                    candidates =
                        candidates
                )
            }
            .sortedWith(
                compareBy<CatalogKnowledgeMatchRequest>(
                    { it.serverArtifact },
                    { it.catalogKey }
                )
            )
            .also { requests ->

                require(
                    requests.all {
                        it.serverArtifact ==
                                NUTRITION_ARTIFACT
                    }
                ) {
                    "Offline nutrition matching received requests " +
                            "for another artifact."
                }
            }
    }

    private fun readDecisions(
        file: File
    ): List<CatalogKnowledgeMatchDecision> {

        if (!file.isFile) {
            return emptyList()
        }

        val root =
            parseJsonObject(
                file = file
            )

        val version =
            root.requiredInt(
                key = "version"
            )

        require(
            version ==
                    CatalogKnowledgeMatchDecisionContract
                        .CURRENT_VERSION
        ) {
            "Unsupported nutrition match decision version: " +
                    version
        }

        val decisions =
            root
                .requiredArray(
                    key = "decisions"
                )
                .map { element ->

                    /*
                     * Gson berücksichtigt auch decisionSource.
                     * Alte JSON-Decisions ohne Feld sollten bereits
                     * durch euren kompatiblen Decision-Reader migriert
                     * beziehungsweise mit CHAT_GPT gelesen werden.
                     */
                    GSON.fromJson(
                        element,
                        CatalogKnowledgeMatchDecision::class.java
                    )
                }
                .sortedWith(
                    CatalogKnowledgeMatchDecisions.DECISION_ORDER
                )

        CatalogKnowledgeMatchDecisions(
            version =
                CatalogKnowledgeMatchDecisionContract
                    .CURRENT_VERSION,
            decisions =
                decisions
        )

        return decisions
    }

    private fun persistDecisions(
        decisions:
        List<CatalogKnowledgeMatchDecision>
    ) {
        val batch =
            CatalogKnowledgeMatchDecisions(
                version =
                    CatalogKnowledgeMatchDecisionContract
                        .CURRENT_VERSION,
                decisions =
                    decisions.sortedWith(
                        CatalogKnowledgeMatchDecisions.DECISION_ORDER
                    )
            )

        writeJsonAtomically(
            value =
                batch,
            file =
                decisionFile
        )
    }

    private fun writeJsonAtomically(
        value: Any,
        file: File
    ) {
        val parentDirectory =
            file.parentFile

        if (
            parentDirectory != null &&
            !parentDirectory.exists()
        ) {
            check(
                parentDirectory.mkdirs()
            ) {
                "Could not create nutrition decision directory: " +
                        parentDirectory.absolutePath
            }
        }

        val temporaryFile =
            File(
                parentDirectory,
                "${file.name}.tmp"
            )

        temporaryFile.writeText(
            GSON.toJson(value) + "\n"
        )

        try {
            Files.move(
                temporaryFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            )

        } catch (
            exception: AtomicMoveNotSupportedException
        ) {
            Files.move(
                temporaryFile.toPath(),
                file.toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        }
    }

    private fun parseJsonObject(
        file: File
    ): JsonObject {

        val element =
            JsonParser.parseString(
                file.readText()
            )

        require(element.isJsonObject) {
            "Expected JSON object in: " +
                    file.absolutePath
        }

        return element.asJsonObject
    }

    private fun JsonObject.requiredString(
        key: String
    ): String {

        return optionalString(
            key = key
        )
            ?: error(
                "Missing or blank string '$key'."
            )
    }

    private fun JsonObject.optionalString(
        key: String
    ): String? {

        return get(key)
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
    }

    private fun JsonObject.requiredInt(
        key: String
    ): Int {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing integer '$key'."
        }

        return element.asInt
    }

    private fun JsonObject.requiredDouble(
        key: String
    ): Double {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing number '$key'."
        }

        return element.asDouble
    }

    private fun JsonObject.requiredArray(
        key: String
    ): JsonArray {

        return get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
            ?: error(
                "Missing JSON array '$key'."
            )
    }

    private fun CatalogKnowledgeMatchRequest.identity():
            MatchIdentity {

        return MatchIdentity(
            catalogKey =
                catalogKey,
            serverArtifact =
                serverArtifact
        )
    }

    private fun CatalogKnowledgeMatchDecision.identity():
            MatchIdentity {

        return MatchIdentity(
            catalogKey =
                catalogKey,
            serverArtifact =
                serverArtifact
        )
    }

    private data class MatchIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )

    private companion object {

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        val GSON: Gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
    }
}