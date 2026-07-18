package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecision
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionContract
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionSource
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisions
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class NutritionUnresolvedDecisionPreparer(
    private val decisionFile: File,
    private val validationReportFile: File
) {

    fun prepare():
            NutritionUnresolvedDecisionPreparationResult {

        val existingDecisions =
            readDecisions(
                file = decisionFile
            )

        if (existingDecisions.isEmpty()) {

            return NutritionUnresolvedDecisionPreparationResult(
                existingDecisionCount = 0,
                acceptedDecisionCount = 0,
                unresolvedDecisionCount = 0,
                retainedDecisionCount = 0,
                acceptedLocalModelDecisionCount =
                    0,
                acceptedChatGptDecisionCount =
                    0,
            )
        }

        require(validationReportFile.isFile) {
            "Nutrition mapping validation report does not exist: " +
                    validationReportFile.absolutePath
        }

        val validationStatuses =
            readValidationStatuses(
                file = validationReportFile
            )

        val duplicateDecisionIdentities =
            existingDecisions
                .groupingBy {
                    it.identity()
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateDecisionIdentities.isEmpty()) {
            "Duplicate nutrition match decisions: " +
                    duplicateDecisionIdentities
                        .sortedWith(
                            compareBy<DecisionIdentity>(
                                { it.serverArtifact },
                                { it.catalogKey }
                            )
                        )
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverArtifact}"
                        }
        }

        val retainedDecisions =
            existingDecisions
                .filter { decision ->

                    validationStatuses[
                        decision.identity()
                    ] == ACCEPTED
                }
                .sortedWith(
                    CatalogKnowledgeMatchDecisions.DECISION_ORDER
                )

        val acceptedLocalModelDecisionCount =
            retainedDecisions.count {
                it.decisionSource ==
                        CatalogKnowledgeMatchDecisionSource.LOCAL_MODEL
            }

        val acceptedChatGptDecisionCount =
            retainedDecisions.count {
                it.decisionSource ==
                        CatalogKnowledgeMatchDecisionSource.CHAT_GPT
            }

        val acceptedDecisionCount =
            retainedDecisions.size

        val unresolvedDecisionCount =
            existingDecisions.size -
                    acceptedDecisionCount

        persistDecisions(
            decisions =
                retainedDecisions
        )

        return NutritionUnresolvedDecisionPreparationResult(
            existingDecisionCount =
                existingDecisions.size,
            acceptedDecisionCount =
                acceptedDecisionCount,
            unresolvedDecisionCount =
                unresolvedDecisionCount,
            retainedDecisionCount =
                retainedDecisions.size,
            acceptedLocalModelDecisionCount =
                acceptedLocalModelDecisionCount,
            acceptedChatGptDecisionCount =
                acceptedChatGptDecisionCount,
        )
    }

    private fun readValidationStatuses(
        file: File
    ): Map<DecisionIdentity, String> {

        val root =
            parseObject(
                file = file
            )

        /*
         * Produktiver Contract:
         *
         * CatalogKnowledgeMappingValidationReport
         *     -> validations[]
         *     -> status
         *
         * diagnostics/validationStatus werden zusätzlich nur als
         * rückwärtskompatible Formate unterstützt.
         */
        val validations =
            root.arrayOrNull(
                key = "validations"
            )
                ?: root.arrayOrNull(
                    key = "diagnostics"
                )
                ?: root.arrayOrNull(
                    key = "entries"
                )
                ?: error(
                    "Nutrition validation report contains none of " +
                            "'validations', 'diagnostics' or 'entries': " +
                            file.absolutePath
                )

        val statuses =
            validations.map { element ->

                require(element.isJsonObject) {
                    "Nutrition validation entry must be a JSON object: " +
                            file.absolutePath
                }

                val validation =
                    element.asJsonObject

                val catalogKey =
                    validation.requiredString(
                        key = "catalogKey"
                    )

                val serverArtifact =
                    validation.optionalString(
                        key = "serverArtifact"
                    )
                        ?: NUTRITION_ARTIFACT

                val status =
                    validation.optionalString(
                        key = "status"
                    )
                        ?: validation.optionalString(
                            key = "validationStatus"
                        )
                        ?: error(
                            "Nutrition validation entry contains neither " +
                                    "'status' nor 'validationStatus': " +
                                    "$catalogKey"
                        )

                DecisionIdentity(
                    catalogKey =
                        catalogKey,
                    serverArtifact =
                        serverArtifact
                ) to status
            }

        val duplicateIdentities =
            statuses
                .groupingBy {
                    it.first
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateIdentities.isEmpty()) {
            "Duplicate nutrition validation identities: " +
                    duplicateIdentities
                        .sortedWith(
                            compareBy<DecisionIdentity>(
                                { it.serverArtifact },
                                { it.catalogKey }
                            )
                        )
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverArtifact}"
                        }
        }

        return statuses
            .toMap()
            .toSortedMap(
                compareBy<DecisionIdentity>(
                    { it.serverArtifact },
                    { it.catalogKey }
                )
            )
    }

    private fun readDecisions(
        file: File
    ): List<CatalogKnowledgeMatchDecision> {

        if (!file.isFile) {
            return emptyList()
        }

        val root =
            parseObject(
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
            root.requiredArray(
                key = "decisions"
            )
                .map { element ->

                    GSON.fromJson(
                        element,
                        CatalogKnowledgeMatchDecision::class.java
                    )
                }
                .sortedWith(
                    CatalogKnowledgeMatchDecisions.DECISION_ORDER
                )

        /*
         * Erzwingt die bestehenden Batch-Invarianten.
         */
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
        decisions: List<CatalogKnowledgeMatchDecision>
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
        val directory =
            file.parentFile

        if (
            directory != null &&
            !directory.exists()
        ) {
            check(directory.mkdirs()) {
                "Could not create decision directory: " +
                        directory.absolutePath
            }
        }

        val temporaryFile =
            File(
                directory,
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

    private fun parseObject(
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

    private fun JsonObject.requiredArray(
        key: String
    ): JsonArray {

        return arrayOrNull(
            key = key
        )
            ?: error(
                "Missing JSON array '$key'."
            )
    }

    private fun JsonObject.arrayOrNull(
        key: String
    ): JsonArray? {

        return get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
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

    private fun CatalogKnowledgeMatchDecision.identity():
            DecisionIdentity {

        return DecisionIdentity(
            catalogKey =
                catalogKey,
            serverArtifact =
                serverArtifact
        )
    }

    private data class DecisionIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )

    private companion object {

        const val ACCEPTED =
            "ACCEPTED"

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        val GSON: Gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
    }
}

data class NutritionUnresolvedDecisionPreparationResult(
    val existingDecisionCount: Int,
    val acceptedDecisionCount: Int,
    val acceptedLocalModelDecisionCount: Int,
    val acceptedChatGptDecisionCount: Int,
    val unresolvedDecisionCount: Int,
    val retainedDecisionCount: Int
)