package de.shopme.tools.knowledge.report

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

/**
 * Baut RuntimeKnowledgeGapInput ausschließlich aus bereits persistierten
 * Matching-Artefakten.
 *
 * Es werden:
 *
 * - keine Kandidaten neu gesucht,
 * - keine AI-Anfragen ausgeführt,
 * - keine Entscheidungen verändert,
 * - keine Mappings geschrieben.
 */
class RuntimeKnowledgeGapInputBuilder(
    private val defaultMinimumConfidence: Double = 0.80
) {

    init {
        require(defaultMinimumConfidence in 0.0..1.0) {
            "defaultMinimumConfidence must be between 0.0 and 1.0."
        }
    }

    fun build(
        catalogFile: File,
        serverArtifactFile: File,
        candidateRetrievalFile: File,
        decisionFile: File,
        validationFile: File?,
        catalogServerMappingFile: File?,
        diagnosticsFile: File? = null
    ): List<RuntimeKnowledgeGapInput> {

        requireFile(
            file = catalogFile,
            description = "Catalog file"
        )

        requireFile(
            file = serverArtifactFile,
            description = "Server artifact file"
        )

        requireFile(
            file = candidateRetrievalFile,
            description = "Candidate retrieval file"
        )

        requireFile(
            file = decisionFile,
            description = "Decision file"
        )

        if (validationFile != null) {
            requireFile(
                file = validationFile,
                description = "Validation file"
            )
        }

        if (catalogServerMappingFile != null) {
            requireFile(
                file = catalogServerMappingFile,
                description = "Catalog-server mapping file"
            )
        }

        if (diagnosticsFile != null) {
            requireFile(
                file = diagnosticsFile,
                description = "Match diagnostics file"
            )
        }

        val catalogKeys =
            readCatalogKeys(
                file = catalogFile
            )

        val serverEntries =
            readServerEntries(
                file = serverArtifactFile
            )

        val mappings =
            readMappings(
                file = catalogServerMappingFile
            )

        val retrievalResults =
            readRetrievalResults(
                file = candidateRetrievalFile
            )

        val decisions =
            readDecisions(
                file = decisionFile
            )

        val validations =
            readValidations(
                file = validationFile
            )

        val diagnostics =
            readDiagnostics(
                file = diagnosticsFile
            )

        val missingCatalogKeys =
            catalogKeys
                .filterNot { catalogKey ->
                    isCovered(
                        catalogKey = catalogKey,
                        mappings = mappings,
                        normalizedServerEntries = serverEntries
                    )
                }
                .sorted()

        return missingCatalogKeys.map { catalogKey ->

            val diagnostic =
                diagnostics[
                    catalogKey
                ]

            val candidates =
                diagnostic
                    ?.candidates
                    ?: retrievalResults[
                        catalogKey
                    ]
                        .orEmpty()

            val decision =
                diagnostic
                    ?.decision
                    ?: decisions[
                        catalogKey
                    ]

            val validation =
                diagnostic
                    ?.validation
                    ?: validations[
                        catalogKey
                    ]

            val selectedServerKey =
                decision
                    ?.selectedServerKey
                    ?.let(::normalizeKey)
                    ?.takeIf(String::isNotBlank)

            RuntimeKnowledgeGapInput(
                catalogKey = catalogKey,
                candidates = candidates,
                decision = decision,
                validation = validation,
                serverEntryExists =
                    when {
                        diagnostic?.mappingWritten == true ->
                            selectedServerKey
                                ?.let(serverEntries::contains)
                                ?: false

                        selectedServerKey != null ->
                            selectedServerKey in serverEntries

                        else ->
                            null
                    }
            )
        }
    }

    private fun isCovered(
        catalogKey: String,
        mappings: Map<String, String>,
        normalizedServerEntries: Set<String>
    ): Boolean {

        if (catalogKey in normalizedServerEntries) {
            return true
        }

        val mappedServerKey =
            mappings[
                catalogKey
            ]
                ?: return false

        return normalizeKey(mappedServerKey) in
                normalizedServerEntries
    }

    private fun readCatalogKeys(
        file: File
    ): Set<String> {

        val root =
            parseJson(
                file = file
            )

        require(root.isJsonArray) {
            "Catalog file must contain a JSON array: ${file.absolutePath}"
        }

        return root.asJsonArray
            .asSequence()
            .mapNotNull { element ->
                element
                    .takeIf(JsonElement::isJsonObject)
                    ?.asJsonObject
                    ?.firstString(
                        "normalizedEnglish",
                        "name",
                        "productName",
                        "title"
                    )
            }
            .map(::normalizeKey)
            .filter(String::isNotBlank)
            .toSortedSet()
    }

    private fun readServerEntries(
        file: File
    ): Set<String> {

        val root =
            parseJsonObject(
                file = file,
                description = "Server artifact"
            )

        val entries =
            root["entries"]
                ?.takeIf(JsonElement::isJsonObject)
                ?.asJsonObject
                ?: return emptySet()

        val normalizedEntries =
            entries
                .entrySet()
                .map { entry ->
                    normalizeKey(
                        value = entry.key
                    )
                }
                .filter(String::isNotBlank)

        requireUniqueKeys(
            keys = normalizedEntries,
            description = "normalized server entry keys"
        )

        return normalizedEntries.toSortedSet()
    }

    private fun readMappings(
        file: File?
    ): Map<String, String> {

        if (file == null) {
            return emptyMap()
        }

        val root =
            parseJsonObject(
                file = file,
                description = "Catalog-server mapping artifact"
            )

        val mappings =
            root.array(
                "mappings"
            )
                .mapNotNull { element ->

                    val mapping =
                        element
                            .takeIf(JsonElement::isJsonObject)
                            ?.asJsonObject
                            ?: return@mapNotNull null

                    val catalogKey =
                        mapping.firstString(
                            "catalogKey",
                            "catalog_key"
                        )
                            ?.let(::normalizeKey)
                            ?.takeIf(String::isNotBlank)
                            ?: return@mapNotNull null

                    val serverKey =
                        mapping.firstString(
                            "serverKey",
                            "server_key",
                            "selectedServerKey"
                        )
                            ?.trim()
                            ?.takeIf(String::isNotBlank)
                            ?: return@mapNotNull null

                    catalogKey to serverKey
                }

        requireUniquePairKeys(
            pairs = mappings,
            description = "catalog-server mappings"
        )

        return mappings
            .toMap()
            .toSortedMap()
    }

    private fun readRetrievalResults(
        file: File
    ): Map<String, List<String>> {

        val objects =
            readArtifactObjects(
                file = file,
                arrayNames =
                    listOf(
                        "retrievalResults",
                        "results",
                        "requests",
                        "items",
                        "entries"
                    )
            )

        val results =
            objects.mapNotNull { item ->

                val catalogKey =
                    item.firstString(
                        "catalogKey",
                        "catalog_key",
                        "query",
                        "normalizedCatalogKey"
                    )
                        ?.let(::normalizeKey)
                        ?.takeIf(String::isNotBlank)
                        ?: return@mapNotNull null

                val candidates =
                    item.firstArray(
                        "candidates",
                        "candidateKeys",
                        "serverCandidates"
                    )
                        .mapNotNull { candidate ->

                            when {
                                candidate.isJsonPrimitive ->
                                    candidate
                                        .asString
                                        .trim()
                                        .takeIf(String::isNotBlank)

                                candidate.isJsonObject ->
                                    candidate
                                        .asJsonObject
                                        .firstString(
                                            "serverKey",
                                            "key",
                                            "canonicalId",
                                            "reference",
                                            "name"
                                        )

                                else ->
                                    null
                            }
                        }
                        .map(::normalizeKey)
                        .filter(String::isNotBlank)
                        .distinct()
                        .sorted()

                catalogKey to candidates
            }

        requireUniquePairKeys(
            pairs = results,
            description = "candidate retrieval results"
        )

        return results
            .toMap()
            .toSortedMap()
    }

    private fun readDecisions(
        file: File
    ): Map<String, RuntimeKnowledgeGapDecision> {

        val objects =
            readArtifactObjects(
                file = file,
                arrayNames =
                    listOf(
                        "decisions",
                        "results",
                        "matches",
                        "items",
                        "entries"
                    )
            )

        val decisions =
            objects.mapNotNull { item ->

                val catalogKey =
                    item.firstString(
                        "catalogKey",
                        "catalog_key",
                        "normalizedCatalogKey"
                    )
                        ?.let(::normalizeKey)
                        ?.takeIf(String::isNotBlank)
                        ?: return@mapNotNull null

                val outcome =
                    parseDecisionOutcome(
                        item = item
                    )
                        ?: return@mapNotNull null

                val confidence =
                    item.firstDouble(
                        "confidence",
                        "score",
                        "matchConfidence"
                    )

                val minimumConfidence =
                    item.firstDouble(
                        "minimumConfidence",
                        "minimum_confidence",
                        "threshold",
                        "confidenceThreshold"
                    )
                        ?: defaultMinimumConfidence

                catalogKey to
                        RuntimeKnowledgeGapDecision(
                            outcome = outcome,
                            selectedServerKey =
                                item.firstString(
                                    "selectedServerKey",
                                    "serverKey",
                                    "selectedCandidate",
                                    "reference",
                                    "match"
                                ),
                            confidence = confidence,
                            minimumConfidence =
                                minimumConfidence
                        )
            }

        requireUniquePairKeys(
            pairs = decisions,
            description = "AI match decisions"
        )

        return decisions
            .toMap()
            .toSortedMap()
    }

    private fun parseDecisionOutcome(
        item: JsonObject
    ): RuntimeKnowledgeGapDecisionOutcome? {

        val rawOutcome =
            item.firstString(
                "outcome",
                "decision",
                "result",
                "status",
                "matchType"
            )
                ?.trim()
                ?.uppercase()
                ?.replace("-", "_")
                ?.replace(" ", "_")

        return when (rawOutcome) {

            "MATCH",
            "MATCHED",
            "ACCEPT",
            "ACCEPTED",
            "YES" ->
                RuntimeKnowledgeGapDecisionOutcome.MATCH

            "NO_MATCH",
            "NOMATCH",
            "REJECT",
            "REJECTED",
            "NONE",
            "NO" ->
                RuntimeKnowledgeGapDecisionOutcome.NO_MATCH

            "AMBIGUOUS",
            "AMBIGUOUS_MATCH",
            "MULTIPLE_MATCHES" ->
                RuntimeKnowledgeGapDecisionOutcome.AMBIGUOUS

            null -> {
                when {
                    item.firstBoolean(
                        "matched",
                        "isMatch"
                    ) == true ->
                        RuntimeKnowledgeGapDecisionOutcome.MATCH

                    item.firstBoolean(
                        "matched",
                        "isMatch"
                    ) == false ->
                        RuntimeKnowledgeGapDecisionOutcome.NO_MATCH

                    else ->
                        null
                }
            }

            else ->
                null
        }
    }

    private fun readValidations(
        file: File?
    ): Map<String, RuntimeKnowledgeGapValidation> {

        if (file == null) {
            return emptyMap()
        }

        val objects =
            readArtifactObjects(
                file = file,
                arrayNames =
                    listOf(
                        "validations",
                        "results",
                        "items",
                        "entries"
                    )
            )

        val validations =
            objects.mapNotNull { item ->

                val catalogKey =
                    item.firstString(
                        "catalogKey",
                        "catalog_key",
                        "normalizedCatalogKey"
                    )
                        ?.let(::normalizeKey)
                        ?.takeIf(String::isNotBlank)
                        ?: return@mapNotNull null

                val accepted =
                    item.firstBoolean(
                        "accepted",
                        "valid",
                        "isValid",
                        "approved"
                    )
                        ?: parseValidationStatus(
                            item.firstString(
                                "status",
                                "result",
                                "validation"
                            )
                        )
                        ?: return@mapNotNull null

                catalogKey to
                        RuntimeKnowledgeGapValidation(
                            accepted = accepted,
                            reason =
                                item.firstString(
                                    "reason",
                                    "message",
                                    "validationReason",
                                    "rejectionReason"
                                )
                        )
            }

        requireUniquePairKeys(
            pairs = validations,
            description = "validation results"
        )

        return validations
            .toMap()
            .toSortedMap()
    }

    private fun parseValidationStatus(
        value: String?
    ): Boolean? =
        when (
            value
                ?.trim()
                ?.uppercase()
                ?.replace("-", "_")
                ?.replace(" ", "_")
        ) {

            "ACCEPTED",
            "APPROVED",
            "VALID",
            "PASS",
            "PASSED" ->
                true

            "REJECTED",
            "INVALID",
            "FAIL",
            "FAILED" ->
                false

            else ->
                null
        }

    private fun readArtifactObjects(
        file: File,
        arrayNames: List<String>
    ): List<JsonObject> {

        val root =
            parseJson(
                file = file
            )

        val elements =
            when {
                root.isJsonArray ->
                    root.asJsonArray.toList()

                root.isJsonObject -> {
                    val rootObject =
                        root.asJsonObject

                    arrayNames
                        .asSequence()
                        .mapNotNull { key ->
                            rootObject[key]
                                ?.takeIf(JsonElement::isJsonArray)
                                ?.asJsonArray
                        }
                        .firstOrNull()
                        ?.toList()
                        ?: emptyList()
                }

                else ->
                    emptyList()
            }

        return elements
            .filter(JsonElement::isJsonObject)
            .map(JsonElement::getAsJsonObject)
    }

    private fun parseJsonObject(
        file: File,
        description: String
    ): JsonObject {

        val root =
            parseJson(
                file = file
            )

        require(root.isJsonObject) {
            "$description must contain a JSON object: ${file.absolutePath}"
        }

        return root.asJsonObject
    }

    private fun parseJson(
        file: File
    ): JsonElement =
        runCatching {
            JsonParser.parseString(
                file.readText()
            )
        }
            .getOrElse { cause ->
                throw IllegalArgumentException(
                    "Could not parse JSON file: ${file.absolutePath}",
                    cause
                )
            }

    private fun requireFile(
        file: File,
        description: String
    ) {
        require(file.isFile) {
            "$description missing: ${file.absolutePath}"
        }
    }

    private fun requireUniqueKeys(
        keys: Collection<String>,
        description: String
    ) {
        val duplicates =
            keys
                .groupingBy { it }
                .eachCount()
                .filterValues { count ->
                    count > 1
                }
                .keys
                .sorted()

        require(duplicates.isEmpty()) {
            "Duplicate $description: $duplicates"
        }
    }

    private fun <T> requireUniquePairKeys(
        pairs: Collection<Pair<String, T>>,
        description: String
    ) {
        requireUniqueKeys(
            keys =
                pairs.map {
                    it.first
                },
            description = description
        )
    }

    private fun JsonObject.array(
        key: String
    ): JsonArray =
        get(key)
            ?.takeIf(JsonElement::isJsonArray)
            ?.asJsonArray
            ?: JsonArray()

    private fun JsonObject.firstArray(
        vararg keys: String
    ): JsonArray =
        keys
            .asSequence()
            .mapNotNull { key ->
                get(key)
                    ?.takeIf(JsonElement::isJsonArray)
                    ?.asJsonArray
            }
            .firstOrNull()
            ?: JsonArray()

    private fun JsonObject.firstString(
        vararg keys: String
    ): String? =
        keys
            .asSequence()
            .mapNotNull { key ->
                get(key)
                    ?.takeIf {
                        !it.isJsonNull &&
                                it.isJsonPrimitive &&
                                it.asJsonPrimitive.isString
                    }
                    ?.asString
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            }
            .firstOrNull()

    private fun JsonObject.firstDouble(
        vararg keys: String
    ): Double? =
        keys
            .asSequence()
            .mapNotNull { key ->
                get(key)
                    ?.takeIf {
                        !it.isJsonNull &&
                                it.isJsonPrimitive
                    }
                    ?.let { value ->
                        runCatching {
                            value.asDouble
                        }.getOrNull()
                    }
            }
            .firstOrNull()

    private fun JsonObject.firstBoolean(
        vararg keys: String
    ): Boolean? =
        keys
            .asSequence()
            .mapNotNull { key ->

                val value =
                    get(key)
                        ?.takeIf {
                            !it.isJsonNull &&
                                    it.isJsonPrimitive
                        }
                        ?: return@mapNotNull null

                when {
                    value.asJsonPrimitive.isBoolean ->
                        value.asBoolean

                    value.asJsonPrimitive.isString ->
                        when (
                            value.asString
                                .trim()
                                .lowercase()
                        ) {
                            "true",
                            "yes",
                            "accepted",
                            "valid" ->
                                true

                            "false",
                            "no",
                            "rejected",
                            "invalid" ->
                                false

                            else ->
                                null
                        }

                    else ->
                        null
                }
            }
            .firstOrNull()

    private fun normalizeKey(
        value: String
    ): String =
        value
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .collapseWhitespace()
            .trim()

    private fun String.collapseWhitespace(): String {

        val builder =
            StringBuilder(length)

        var previousWasWhitespace =
            false

        for (character in this) {

            if (character.isWhitespace()) {

                if (!previousWasWhitespace) {
                    builder.append(' ')
                }

                previousWasWhitespace =
                    true

            } else {
                builder.append(character)

                previousWasWhitespace =
                    false
            }
        }

        return builder.toString()
    }

    private fun readDiagnostics(
        file: File?
    ): Map<String, PersistedMatchDiagnostic> {

        if (file == null) {
            return emptyMap()
        }

        val root =
            parseJsonObject(
                file = file,
                description =
                    "Catalog knowledge match diagnostics"
            )

        val version =
            root["version"]
                ?.takeIf {
                    !it.isJsonNull &&
                            it.isJsonPrimitive &&
                            it.asJsonPrimitive.isNumber
                }
                ?.asInt
                ?: error(
                    "Match diagnostics version missing: " +
                            file.absolutePath
                )

        require(version == 1) {
            "Unsupported match diagnostics version: $version"
        }

        val diagnosticsArray =
            root["diagnostics"]
                ?.takeIf(JsonElement::isJsonArray)
                ?.asJsonArray
                ?: error(
                    "Match diagnostics array missing: " +
                            file.absolutePath
                )

        val diagnostics =
            diagnosticsArray
                .mapNotNull { element ->

                    val item =
                        element
                            .takeIf(JsonElement::isJsonObject)
                            ?.asJsonObject
                            ?: return@mapNotNull null

                    val catalogKey =
                        item.firstString(
                            "catalogKey"
                        )
                            ?.let(::normalizeKey)
                            ?.takeIf(String::isNotBlank)
                            ?: return@mapNotNull null

                    val candidates =
                        item.firstArray(
                            "candidateServerKeys"
                        )
                            .mapNotNull { candidate ->
                                candidate
                                    .takeIf(JsonElement::isJsonPrimitive)
                                    ?.asString
                                    ?.trim()
                                    ?.takeIf(String::isNotBlank)
                            }
                            .map(::normalizeKey)
                            .filter(String::isNotBlank)
                            .distinct()
                            .sorted()

                    val decisionType =
                        item.firstString(
                            "decisionType"
                        )
                            ?.trim()
                            ?.uppercase()
                            ?.replace("-", "_")
                            ?.replace(" ", "_")

                    val validationStatus =
                        item.firstString(
                            "validationStatus"
                        )
                            ?.trim()
                            ?.uppercase()
                            ?.replace("-", "_")
                            ?.replace(" ", "_")

                    val confidence =
                        item.firstDouble(
                            "confidence"
                        )

                    val decision =
                        createDiagnosticDecision(
                            decisionType =
                                decisionType,
                            validationStatus =
                                validationStatus,
                            selectedServerKey =
                                item.firstString(
                                    "selectedServerKey"
                                ),
                            confidence =
                                confidence
                        )

                    val validation =
                        createDiagnosticValidation(
                            validationStatus =
                                validationStatus,
                            validationReason =
                                item.firstString(
                                    "validationReason"
                                ),
                            decisionReason =
                                item.firstString(
                                    "decisionReason"
                                )
                        )

                    val mappingWritten =
                        item.firstBoolean(
                            "mappingWritten"
                        )
                            ?: false

                    catalogKey to
                            PersistedMatchDiagnostic(
                                candidates =
                                    candidates,
                                decision =
                                    decision,
                                validation =
                                    validation,
                                mappingWritten =
                                    mappingWritten
                            )
                }

        requireUniquePairKeys(
            pairs = diagnostics,
            description =
                "catalog knowledge match diagnostics"
        )

        return diagnostics
            .toMap()
            .toSortedMap()
    }

    private fun createDiagnosticDecision(
        decisionType: String?,
        validationStatus: String?,
        selectedServerKey: String?,
        confidence: Double?
    ): RuntimeKnowledgeGapDecision? {

        val outcome =
            when {

                validationStatus ==
                        "REJECTED_NO_MATCH" ->
                    RuntimeKnowledgeGapDecisionOutcome.NO_MATCH

                decisionType in
                        setOf(
                            "NO_MATCH",
                            "NOMATCH",
                            "REJECT",
                            "REJECTED"
                        ) ->
                    RuntimeKnowledgeGapDecisionOutcome.NO_MATCH

                decisionType in
                        setOf(
                            "AMBIGUOUS",
                            "AMBIGUOUS_MATCH",
                            "MULTIPLE_MATCHES"
                        ) ->
                    RuntimeKnowledgeGapDecisionOutcome.AMBIGUOUS

                decisionType in
                        setOf(
                            "MATCH",
                            "MATCHED",
                            "SELECT",
                            "SELECTED",
                            "ACCEPT",
                            "ACCEPTED"
                        ) ->
                    RuntimeKnowledgeGapDecisionOutcome.MATCH

                validationStatus ==
                        "REJECTED_LOW_CONFIDENCE" &&
                        selectedServerKey != null ->
                    RuntimeKnowledgeGapDecisionOutcome.MATCH

                else ->
                    null
            }
                ?: return null

        return RuntimeKnowledgeGapDecision(
            outcome = outcome,
            selectedServerKey =
                selectedServerKey
                    ?.trim()
                    ?.takeIf(String::isNotBlank),
            confidence = confidence,
            minimumConfidence =
                when {
                    validationStatus ==
                            "REJECTED_LOW_CONFIDENCE" ->
                        DEFAULT_MINIMUM_CONFIDENCE

                    outcome ==
                            RuntimeKnowledgeGapDecisionOutcome.MATCH ->
                        DEFAULT_MINIMUM_CONFIDENCE

                    else ->
                        null
                }
        )
    }

    private fun createDiagnosticValidation(
        validationStatus: String?,
        validationReason: String?,
        decisionReason: String?
    ): RuntimeKnowledgeGapValidation? {

        val accepted =
            when {

                validationStatus == null ->
                    return null

                validationStatus == "ACCEPTED" ||
                        validationStatus.startsWith(
                            prefix = "ACCEPTED_"
                        ) ->
                    true

                validationStatus == "REJECTED" ||
                        validationStatus.startsWith(
                            prefix = "REJECTED_"
                        ) ->
                    false

                else ->
                    return null
            }

        return RuntimeKnowledgeGapValidation(
            accepted = accepted,
            reason =
                validationReason
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
                    ?: decisionReason
                        ?.trim()
                        ?.takeIf(String::isNotBlank)
        )
    }

    private companion object {

        const val DEFAULT_MINIMUM_CONFIDENCE =
            0.80
    }

    private data class PersistedMatchDiagnostic(
        val candidates: List<String>,
        val decision: RuntimeKnowledgeGapDecision?,
        val validation: RuntimeKnowledgeGapValidation?,
        val mappingWritten: Boolean
    )
}