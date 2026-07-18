package de.shopme.tools.knowledge.rebuild.nutrition.diagnostics

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionSource
import java.io.File

class NutritionMatchPersistenceDiagnosticRunner(
    private val decisionFile: File,
    private val validationFile: File,
    private val mappingFile: File,
    private val runtimeNutritionFile: File,
    private val coverageGapReportFile: File? = null
) {

    fun run():
            NutritionMatchPersistenceDiagnosticReport {

        val matchDecisions =
            readMatchDecisions(
                file =
                    decisionFile
            )

        val validationsByCatalogKey =
            readValidationRecords(
                file =
                    validationFile
            )
                .associateBy {
                    it.catalogKey
                }

        val mappingsByCatalogKey =
            readMappings(
                file =
                    mappingFile
            )
                .associateBy {
                    it.catalogKey
                }

        val runtimeCatalogKeys =
            readRuntimeCatalogKeys(
                file =
                    runtimeNutritionFile
            )

        val diagnostics =
            matchDecisions
                .map { decision ->

                    val validation =
                        validationsByCatalogKey[
                            decision.catalogKey
                        ]

                    val mapping =
                        mappingsByCatalogKey[
                            decision.catalogKey
                        ]

                    val runtimePresent =
                        decision.catalogKey in
                                runtimeCatalogKeys

                    val firstMissingStage =
                        determineFirstMissingStage(
                            validation =
                                validation,
                            mapping =
                                mapping,
                            runtimePresent =
                                runtimePresent
                        )

                    NutritionMatchPersistenceDiagnostic(
                        catalogKey =
                            decision.catalogKey,
                        selectedServerKey =
                            decision.selectedServerKey,
                        decisionSource =
                            decision.decisionSource,
                        decisionConfidence =
                            decision.confidence,
                        validationRecordPresent =
                            validation != null,
                        validationAccepted =
                            validation?.accepted,
                        validationStatus =
                            validation?.status,
                        mappingPresent =
                            mapping != null,
                        mappedServerKey =
                            mapping?.serverKey,
                        runtimePresent =
                            runtimePresent,
                        firstMissingStage =
                            firstMissingStage,
                        details =
                            buildDetails(
                                decision =
                                    decision,
                                validation =
                                    validation,
                                mapping =
                                    mapping,
                                runtimePresent =
                                    runtimePresent,
                                firstMissingStage =
                                    firstMissingStage
                            )
                    )
                }
                .sortedBy {
                    it.catalogKey
                }

        val expectedMatchNotPersistedCount =
            coverageGapReportFile
                ?.takeIf {
                    it.isFile
                }
                ?.let(
                    ::readExpectedMatchNotPersistedCount
                )

        return NutritionMatchPersistenceDiagnosticReport(
            version =
                NutritionMatchPersistenceDiagnosticReport
                    .CURRENT_VERSION,
            matchDecisionCount =
                diagnostics.size,
            validationRecordCount =
                diagnostics.count {
                    it.validationRecordPresent
                },
            explicitlyAcceptedValidationCount =
                diagnostics.count {
                    it.validationAccepted == true
                },
            explicitlyRejectedValidationCount =
                diagnostics.count {
                    it.validationAccepted == false
                },
            mappingPresentCount =
                diagnostics.count {
                    it.mappingPresent
                },
            runtimePresentCount =
                diagnostics.count {
                    it.runtimePresent
                },
            fullyPersistedCount =
                diagnostics.count {
                    it.fullyPersisted
                },
            missingPersistenceCount =
                diagnostics.count {
                    !it.fullyPersisted
                },
            countsByFirstMissingStage =
                diagnostics
                    .groupingBy {
                        it.firstMissingStage.name
                    }
                    .eachCount()
                    .toSortedMap(),
            expectedMatchNotPersistedCount =
                expectedMatchNotPersistedCount,
            diagnostics =
                diagnostics
        )
    }

    private fun determineFirstMissingStage(
        validation: PersistedValidation?,
        mapping: PersistedMapping?,
        runtimePresent: Boolean
    ): NutritionMatchPersistenceMissingStage {

        if (
            validation != null &&
            validation.accepted == false
        ) {
            return NutritionMatchPersistenceMissingStage
                .VALIDATION_REJECTED
        }

        if (mapping == null) {
            return NutritionMatchPersistenceMissingStage.MAPPING
        }

        if (!runtimePresent) {
            return NutritionMatchPersistenceMissingStage.RUNTIME
        }

        return NutritionMatchPersistenceMissingStage.NONE
    }

    private fun buildDetails(
        decision: PersistedMatchDecision,
        validation: PersistedValidation?,
        mapping: PersistedMapping?,
        runtimePresent: Boolean,
        firstMissingStage: NutritionMatchPersistenceMissingStage
    ): String {

        return when (firstMissingStage) {

            NutritionMatchPersistenceMissingStage.NONE ->
                "MATCH decision is present in the central mapping " +
                        "artifact and in the runtime nutrition artifact."

            NutritionMatchPersistenceMissingStage
                .VALIDATION_REJECTED ->
                "MATCH decision was explicitly rejected by persisted " +
                        "validation status '${validation?.status}'."

            NutritionMatchPersistenceMissingStage.MAPPING ->
                if (validation == null) {
                    "MATCH decision selects " +
                            "'${decision.selectedServerKey}', but no " +
                            "central catalog-server mapping exists. " +
                            "No explicit representative-validation " +
                            "record was found."
                } else {
                    "MATCH decision selects " +
                            "'${decision.selectedServerKey}', " +
                            "validation status is " +
                            "'${validation.status}', but no central " +
                            "catalog-server mapping exists."
                }

            NutritionMatchPersistenceMissingStage.RUNTIME ->
                "Central mapping points to " +
                        "'${mapping?.serverKey}', but the catalog key " +
                        "is absent from the runtime nutrition artifact. " +
                        "runtimePresent=$runtimePresent."
        }
    }

    private fun readMatchDecisions(
        file: File
    ): List<PersistedMatchDecision> {

        require(file.isFile) {
            "Nutrition decision file does not exist: " +
                    file.absolutePath
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
            root.asJsonObject
                .arrayOrNull(
                    key =
                        "decisions"
                )
                ?: error(
                    "Nutrition decision file contains no 'decisions' " +
                            "array: " +
                            file.absolutePath
                )

        val parsed =
            decisions.mapNotNull { element ->

                require(element.isJsonObject) {
                    "Nutrition decision entry must be a JSON object."
                }

                val decision =
                    element.asJsonObject

                val serverArtifact =
                    decision.requiredString(
                        key =
                            "serverArtifact"
                    )

                if (
                    serverArtifact !=
                    NUTRITION_ARTIFACT
                ) {
                    return@mapNotNull null
                }

                val type =
                    decision.requiredString(
                        key =
                            "type"
                    )

                if (type != MATCH_DECISION_TYPE) {
                    return@mapNotNull null
                }

                PersistedMatchDecision(
                    catalogKey =
                        normalizeKey(
                            decision.requiredString(
                                key =
                                    "catalogKey"
                            )
                        ),
                    selectedServerKey =
                        normalizeKey(
                            decision.requiredString(
                                key =
                                    "selectedServerKey"
                            )
                        ),
                    confidence =
                        decision.requiredDouble(
                            key =
                                "confidence"
                        ),
                    decisionSource =
                        decision.optionalString(
                            key =
                                "decisionSource"
                        )
                            ?: CatalogKnowledgeMatchDecisionSource
                                .CHAT_GPT
                                .name
                )
            }
                .sortedBy {
                    it.catalogKey
                }

        requireNoDuplicateCatalogKeys(
            catalogKeys =
                parsed.map {
                    it.catalogKey
                },
            sourceName =
                "nutrition MATCH decisions"
        )

        return parsed
    }

    private fun readValidationRecords(
        file: File
    ): List<PersistedValidation> {

        if (!file.isFile) {
            return emptyList()
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        val entries =
            findArrayContainer(
                root =
                    root,
                candidateKeys =
                    VALIDATION_CONTAINER_KEYS
            )
                ?: return emptyList()

        val parsed =
            entries.mapNotNull { element ->

                if (!element.isJsonObject) {
                    return@mapNotNull null
                }

                val validation =
                    element.asJsonObject

                val catalogKey =
                    validation.firstString(
                        keys =
                            CATALOG_KEY_FIELDS
                    )
                        ?.let(
                            ::normalizeKey
                        )
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: return@mapNotNull null

                val status =
                    validation.firstString(
                        keys =
                            VALIDATION_STATUS_FIELDS
                    )
                        ?.trim()
                        ?.uppercase()

                val accepted =
                    validation.firstBoolean(
                        keys =
                            VALIDATION_BOOLEAN_FIELDS
                    )
                        ?: status?.let(
                            ::statusToAcceptance
                        )

                PersistedValidation(
                    catalogKey =
                        catalogKey,
                    accepted =
                        accepted,
                    status =
                        status
                            ?: when (accepted) {
                                true -> "ACCEPTED"
                                false -> "REJECTED"
                                null -> "UNKNOWN"
                            }
                )
            }
                .sortedBy {
                    it.catalogKey
                }

        requireNoDuplicateCatalogKeys(
            catalogKeys =
                parsed.map {
                    it.catalogKey
                },
            sourceName =
                "nutrition validation records"
        )

        return parsed
    }

    private fun readMappings(
        file: File
    ): List<PersistedMapping> {

        require(file.isFile) {
            "Catalog-server mapping file does not exist: " +
                    file.absolutePath
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Catalog-server mapping file must contain a JSON object: " +
                    file.absolutePath
        }

        val mappings =
            root.asJsonObject
                .arrayOrNull(
                    key =
                        "mappings"
                )
                ?: error(
                    "Catalog-server mapping file contains no " +
                            "'mappings' array: " +
                            file.absolutePath
                )

        val parsed =
            mappings.mapNotNull { element ->

                require(element.isJsonObject) {
                    "Catalog-server mapping entry must be a JSON " +
                            "object."
                }

                val mapping =
                    element.asJsonObject

                val sourceArtifact =
                    mapping.optionalString(
                        key =
                            "sourceArtifact"
                    )
                        ?: mapping.optionalString(
                            key =
                                "serverArtifact"
                        )
                        ?: NUTRITION_ARTIFACT

                if (
                    sourceArtifact !=
                    NUTRITION_ARTIFACT
                ) {
                    return@mapNotNull null
                }

                PersistedMapping(
                    catalogKey =
                        normalizeKey(
                            mapping.requiredString(
                                key =
                                    "catalogKey"
                            )
                        ),
                    serverKey =
                        normalizeKey(
                            mapping.requiredString(
                                key =
                                    "serverKey"
                            )
                        )
                )
            }
                .sortedBy {
                    it.catalogKey
                }

        requireNoDuplicateCatalogKeys(
            catalogKeys =
                parsed.map {
                    it.catalogKey
                },
            sourceName =
                "nutrition catalog-server mappings"
        )

        return parsed
    }

    private fun readRuntimeCatalogKeys(
        file: File
    ): Set<String> {

        require(file.isFile) {
            "Runtime nutrition artifact does not exist: " +
                    file.absolutePath
        }

        val root =
            JsonParser.parseString(
                file.readText()
            )

        return extractRuntimeCatalogKeys(
            element =
                root
        )
            .map(
                ::normalizeKey
            )
            .filter {
                it.isNotBlank()
            }
            .toSortedSet()
    }

    private fun extractRuntimeCatalogKeys(
        element: JsonElement
    ): Set<String> {

        return when {

            element.isJsonArray ->
                element.asJsonArray
                    .mapNotNull { entry ->

                        if (!entry.isJsonObject) {
                            return@mapNotNull null
                        }

                        entry.asJsonObject.firstString(
                            keys =
                                RUNTIME_KEY_FIELDS
                        )
                    }
                    .toSet()

            element.isJsonObject -> {

                val objectValue =
                    element.asJsonObject

                val container =
                    RUNTIME_CONTAINER_KEYS
                        .asSequence()
                        .mapNotNull { key ->

                            objectValue.get(key)
                                ?.takeIf {
                                    !it.isJsonNull &&
                                            (
                                                    it.isJsonArray ||
                                                            it.isJsonObject
                                                    )
                                }
                        }
                        .firstOrNull()

                if (container != null) {
                    extractRuntimeCatalogKeys(
                        element =
                            container
                    )
                } else {
                    objectValue
                        .entrySet()
                        .asSequence()
                        .filter {
                            it.key !in
                                    RUNTIME_METADATA_KEYS
                        }
                        .map {
                            it.key
                        }
                        .toSet()
                }
            }

            else ->
                emptySet()
        }
    }

    private fun readExpectedMatchNotPersistedCount(
        file: File
    ): Int {

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Nutrition coverage-gap report must contain a JSON " +
                    "object: " +
                    file.absolutePath
        }

        val countsByType =
            root.asJsonObject["countsByType"]
                ?.takeIf {
                    it.isJsonObject
                }
                ?.asJsonObject
                ?: error(
                    "Nutrition coverage-gap report contains no " +
                            "'countsByType' object: " +
                            file.absolutePath
                )

        return countsByType[
            MATCH_NOT_PERSISTED_TYPE
        ]
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asInt
            ?: 0
    }

    private fun statusToAcceptance(
        status: String
    ): Boolean? {

        return when (
            status
                .trim()
                .uppercase()
        ) {

            "ACCEPTED",
            "VALID",
            "VALIDATED",
            "MATCH",
            "REPRESENTATIVE",
            "APPROVED" ->
                true

            "REJECTED",
            "INVALID",
            "NO_MATCH",
            "DECLINED" ->
                false

            else ->
                null
        }
    }

    private fun findArrayContainer(
        root: JsonElement,
        candidateKeys: List<String>
    ): JsonArray? {

        if (root.isJsonArray) {
            return root.asJsonArray
        }

        if (!root.isJsonObject) {
            return null
        }

        val objectValue =
            root.asJsonObject

        return candidateKeys
            .asSequence()
            .mapNotNull { key ->

                objectValue.get(key)
                    ?.takeIf {
                        it.isJsonArray
                    }
                    ?.asJsonArray
            }
            .firstOrNull()
    }

    private fun requireNoDuplicateCatalogKeys(
        catalogKeys: List<String>,
        sourceName: String
    ) {
        val duplicates =
            catalogKeys
                .groupingBy {
                    it
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "$sourceName contain duplicate catalog keys: " +
                    duplicates
                        .sorted()
                        .take(MAX_DIAGNOSTIC_KEYS)
                        .joinToString()
        }
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
            key =
                key
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

    private fun JsonObject.requiredDouble(
        key: String
    ): Double {

        return get(key)
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asDouble
            ?: error(
                "Missing numeric value '$key'."
            )
    }

    private fun JsonObject.firstString(
        keys: List<String>
    ): String? {

        return keys
            .asSequence()
            .mapNotNull { key ->

                optionalString(
                    key =
                        key
                )
            }
            .firstOrNull()
    }

    private fun JsonObject.firstBoolean(
        keys: List<String>
    ): Boolean? {

        return keys
            .asSequence()
            .mapNotNull { key ->

                get(key)
                    ?.takeIf {
                        !it.isJsonNull &&
                                it.isJsonPrimitive &&
                                it.asJsonPrimitive.isBoolean
                    }
                    ?.asBoolean
            }
            .firstOrNull()
    }

    private fun normalizeKey(
        value: String
    ): String {

        return value
            .trim()
            .lowercase()
            .replace(
                "-",
                " "
            )
            .replace(
                "_",
                " "
            )
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private data class PersistedMatchDecision(
        val catalogKey: String,
        val selectedServerKey: String,
        val confidence: Double,
        val decisionSource: String
    )

    private data class PersistedValidation(
        val catalogKey: String,
        val accepted: Boolean?,
        val status: String
    )

    private data class PersistedMapping(
        val catalogKey: String,
        val serverKey: String
    )

    private companion object {

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        const val MATCH_DECISION_TYPE =
            "MATCH"

        const val MATCH_NOT_PERSISTED_TYPE =
            "MATCH_NOT_PERSISTED"

        const val MAX_DIAGNOSTIC_KEYS =
            10

        val WHITESPACE_REGEX =
            Regex("\\s+")

        val VALIDATION_CONTAINER_KEYS =
            listOf(
                "validations",
                "results",
                "decisions",
                "entries",
                "items",
                "mappings"
            )

        val CATALOG_KEY_FIELDS =
            listOf(
                "catalogKey",
                "normalizedCatalogKey",
                "sourceKey"
            )

        val VALIDATION_STATUS_FIELDS =
            listOf(
                "status",
                "validationStatus",
                "result",
                "decisionType",
                "type"
            )

        val VALIDATION_BOOLEAN_FIELDS =
            listOf(
                "accepted",
                "valid",
                "validated",
                "isAccepted",
                "isValid"
            )

        val RUNTIME_CONTAINER_KEYS =
            listOf(
                "entries",
                "items",
                "foods",
                "nutrition",
                "knowledge",
                "data",
                "values"
            )

        val RUNTIME_KEY_FIELDS =
            listOf(
                "catalogKey",
                "normalizedEnglish",
                "normalized",
                "canonicalKey",
                "canonicalId",
                "id",
                "key",
                "name"
            )

        val RUNTIME_METADATA_KEYS =
            setOf(
                "version",
                "artifact",
                "artifactType",
                "generatedAt",
                "generatedAtEpochMillis",
                "metadata",
                "summary",
                "schema",
                "source",
                "sources",
                "count",
                "entryCount"
            )
    }
}