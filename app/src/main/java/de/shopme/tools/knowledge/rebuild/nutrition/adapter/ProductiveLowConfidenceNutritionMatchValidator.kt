package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.CatalogKnowledgeMatchDecisionType
import de.shopme.tools.knowledge.mapping.catalog.representative.DeterministicRepresentativeNutritionMappingValidator
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecision
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingRequest
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Locale

class ProductiveLowConfidenceNutritionMatchValidator(
    private val requestFile: File,
    private val decisionFile: File,
    private val representativeValidationFile: File,
    private val minimumConfidence: Double,
    private val validator:
    DeterministicRepresentativeNutritionMappingValidator =
        DeterministicRepresentativeNutritionMappingValidator()
) {

    fun run():
            ProductiveLowConfidenceNutritionMatchValidationResult {

        require(minimumConfidence in 0.0..1.0) {
            "minimumConfidence must be between 0.0 and 1.0."
        }

        require(requestFile.isFile) {
            "Nutrition match request file does not exist: " +
                    requestFile.absolutePath
        }

        require(decisionFile.isFile) {
            "Nutrition match decision file does not exist: " +
                    decisionFile.absolutePath
        }

        require(representativeValidationFile.isFile) {
            "Representative nutrition validation file does not exist: " +
                    representativeValidationFile.absolutePath
        }

        val requestsByIdentity =
            readRequests(
                file =
                    requestFile
            )
                .associateBy {
                    MatchIdentity(
                        catalogKey =
                            normalizeKey(
                                it.catalogKey
                            ),
                        serverArtifact =
                            it.serverArtifact
                    )
                }

        val lowConfidenceMatches =
            readDecisions(
                file =
                    decisionFile
            )
                .filter { decision ->

                    decision.serverArtifact ==
                            NUTRITION_ARTIFACT &&
                            decision.type ==
                            CatalogKnowledgeMatchDecisionType.MATCH &&
                            decision.confidence <
                            minimumConfidence
                }
                .sortedBy {
                    normalizeKey(
                        it.catalogKey
                    )
                }

        val validationArtifact =
            readValidationArtifact(
                file =
                    representativeValidationFile
            )

        val existingEntriesByCatalogKey =
            validationArtifact.entries
                .associateBy { entry ->

                    normalizeKey(
                        entry.requiredString(
                            key =
                                "catalogKey"
                        )
                    )
                }

        val duplicateExistingCatalogKeys =
            validationArtifact.entries
                .groupingBy { entry ->

                    normalizeKey(
                        entry.requiredString(
                            key =
                                "catalogKey"
                        )
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateExistingCatalogKeys.isEmpty()) {
            "Representative nutrition validation file contains " +
                    "duplicate catalog keys: " +
                    duplicateExistingCatalogKeys
                        .sorted()
                        .take(MAX_DIAGNOSTIC_KEYS)
                        .joinToString()
        }

        val newEntries =
            lowConfidenceMatches
                .filterNot { decision ->

                    normalizeKey(
                        decision.catalogKey
                    ) in
                            existingEntriesByCatalogKey
                }
                .map { decision ->

                    val identity =
                        MatchIdentity(
                            catalogKey =
                                normalizeKey(
                                    decision.catalogKey
                                ),
                            serverArtifact =
                                decision.serverArtifact
                        )

                    val request =
                        requestsByIdentity[
                            identity
                        ]
                            ?: error(
                                "No nutrition match request exists for " +
                                        "low-confidence MATCH decision: " +
                                        "${decision.catalogKey} @ " +
                                        decision.serverArtifact
                            )

                    createValidationEntry(
                        decision =
                            decision,
                        request =
                            request
                    )
                }
                .sortedBy { entry ->

                    normalizeKey(
                        entry.requiredString(
                            key =
                                "catalogKey"
                        )
                    )
                }

        val finalEntries =
            (
                    validationArtifact.entries +
                            newEntries
                    )
                .sortedBy { entry ->

                    normalizeKey(
                        entry.requiredString(
                            key =
                                "catalogKey"
                        )
                    )
                }

        val duplicateFinalCatalogKeys =
            finalEntries
                .groupingBy { entry ->

                    normalizeKey(
                        entry.requiredString(
                            key =
                                "catalogKey"
                        )
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateFinalCatalogKeys.isEmpty()) {
            "Merged representative nutrition validation entries " +
                    "contain duplicate catalog keys: " +
                    duplicateFinalCatalogKeys
                        .sorted()
                        .take(MAX_DIAGNOSTIC_KEYS)
                        .joinToString()
        }

        if (newEntries.isNotEmpty()) {
            writeValidationArtifact(
                version =
                    validationArtifact.version,
                entries =
                    finalEntries,
                file =
                    representativeValidationFile
            )
        }

        val newAcceptedCount =
            newEntries.count { entry ->

                entry.requiredBoolean(
                    key =
                        "accepted"
                )
            }

        val result =
            ProductiveLowConfidenceNutritionMatchValidationResult(
                lowConfidenceMatchCount =
                    lowConfidenceMatches.size,
                existingValidationCount =
                    lowConfidenceMatches.size -
                            newEntries.size,
                newlyValidatedCount =
                    newEntries.size,
                newlyAcceptedCount =
                    newAcceptedCount,
                newlyRejectedCount =
                    newEntries.size -
                            newAcceptedCount,
                finalValidationEntryCount =
                    finalEntries.size,
                validationFile =
                    representativeValidationFile.absolutePath
            )

        require(
            result.existingValidationCount +
                    result.newlyValidatedCount ==
                    result.lowConfidenceMatchCount
        ) {
            "Existing and newly validated low-confidence decisions " +
                    "do not cover all low-confidence MATCH decisions."
        }

        require(
            result.newlyAcceptedCount +
                    result.newlyRejectedCount ==
                    result.newlyValidatedCount
        ) {
            "Accepted and rejected validation outcomes do not cover " +
                    "all newly validated decisions."
        }

        return result
    }

    private fun createValidationEntry(
        decision: PersistedDecision,
        request: PersistedRequest
    ): JsonObject {

        val selectedServerKey =
            requireNotNull(
                decision.selectedServerKey
            ) {
                "Low-confidence MATCH decision has no selectedServerKey: " +
                        decision.catalogKey
            }

        val selectedCandidateIndex =
            request.candidates
                .indexOfFirst { candidate ->

                    normalizeKey(
                        candidate.serverKey
                    ) ==
                            normalizeKey(
                                selectedServerKey
                            )
                }

        require(selectedCandidateIndex >= 0) {
            "Selected server key is not part of the persisted request " +
                    "candidates: catalogKey='${decision.catalogKey}', " +
                    "selectedServerKey='$selectedServerKey'."
        }

        val selectedCandidate =
            request.candidates[
                selectedCandidateIndex
            ]

        val representativeRequest =
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    decision.catalogKey,
                serverKey =
                    selectedServerKey,
                confidence =
                    decision.confidence,
                candidateRank =
                    selectedCandidateIndex +
                            1,
                diagnosticScore =
                    selectedCandidate.diagnosticScore,
                sharedTokens =
                    selectedCandidate.sharedTokens
            )

        val representativeDecision =
            validator.validate(
                request =
                    representativeRequest
            )

        return createValidationJson(
            originalDecision =
                decision,
            request =
                representativeRequest,
            representativeDecision =
                representativeDecision
        )
    }

    private fun createValidationJson(
        originalDecision: PersistedDecision,
        request: RepresentativeNutritionMappingRequest,
        representativeDecision:
        RepresentativeNutritionMappingDecision
    ): JsonObject {

        return JsonObject().apply {

            addProperty(
                "catalogKey",
                normalizeKey(
                    originalDecision.catalogKey
                )
            )

            addProperty(
                "selectedServerKey",
                normalizeKey(
                    request.serverKey
                )
            )

            addProperty(
                "candidateRank",
                request.candidateRank
            )

            addProperty(
                "originalConfidence",
                originalDecision.confidence
            )

            addProperty(
                "originalDecisionReason",
                originalDecision.reason
            )

            addProperty(
                "originalValidationStatus",
                REJECTED_LOW_CONFIDENCE_STATUS
            )

            addProperty(
                "originalValidationReason",
                "Decision confidence " +
                        formatConfidence(
                            originalDecision.confidence
                        ) +
                        " is below minimum " +
                        formatConfidence(
                            minimumConfidence
                        )
            )

            addProperty(
                "decisionType",
                representativeDecision.type.name
            )

            val reasonsJson =
                JsonArray()

            representativeDecision.reasons
                .forEach { reason ->

                    reasonsJson.add(
                        reason.name
                    )
                }

            add(
                "reasons",
                reasonsJson
            )

            addProperty(
                "accepted",
                representativeDecision.accepted
            )
        }
    }

    private fun readRequests(
        file: File
    ): List<PersistedRequest> {

        val root =
            parseObject(
                file =
                    file
            )

        val requests =
            root.requiredArray(
                key =
                    "requests"
            )
                .map { element ->

                    require(element.isJsonObject) {
                        "Nutrition match request entry must be a JSON " +
                                "object."
                    }

                    val request =
                        element.asJsonObject

                    PersistedRequest(
                        catalogKey =
                            request.requiredString(
                                key =
                                    "catalogKey"
                            ),
                        serverArtifact =
                            request.requiredString(
                                key =
                                    "serverArtifact"
                            ),
                        candidates =
                            request.requiredArray(
                                key =
                                    "candidates"
                            )
                                .map { candidateElement ->

                                    require(candidateElement.isJsonObject) {
                                        "Nutrition match candidate must " +
                                                "be a JSON object."
                                    }

                                    val candidate =
                                        candidateElement.asJsonObject

                                    PersistedCandidate(
                                        serverKey =
                                            candidate.requiredString(
                                                key =
                                                    "serverKey"
                                            ),
                                        diagnosticScore =
                                            candidate.requiredDouble(
                                                key =
                                                    "diagnosticScore"
                                            ),
                                        sharedTokens =
                                            candidate.requiredArray(
                                                key =
                                                    "sharedTokens"
                                            )
                                                .map { tokenElement ->

                                                    require(
                                                        tokenElement
                                                            .isJsonPrimitive &&
                                                                tokenElement
                                                                    .asJsonPrimitive
                                                                    .isString
                                                    ) {
                                                        "sharedTokens must " +
                                                                "contain " +
                                                                "strings."
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
                    )
                }

        requireNoDuplicateIdentities(
            identities =
                requests.map {
                    MatchIdentity(
                        catalogKey =
                            normalizeKey(
                                it.catalogKey
                            ),
                        serverArtifact =
                            it.serverArtifact
                    )
                },
            sourceName =
                "nutrition match requests"
        )

        return requests
    }

    private fun readDecisions(
        file: File
    ): List<PersistedDecision> {

        val root =
            parseObject(
                file =
                    file
            )

        val decisions =
            root.requiredArray(
                key =
                    "decisions"
            )
                .map { element ->

                    require(element.isJsonObject) {
                        "Nutrition match decision entry must be a JSON " +
                                "object."
                    }

                    val decision =
                        element.asJsonObject

                    PersistedDecision(
                        catalogKey =
                            decision.requiredString(
                                key =
                                    "catalogKey"
                            ),
                        serverArtifact =
                            decision.requiredString(
                                key =
                                    "serverArtifact"
                            ),
                        type =
                            CatalogKnowledgeMatchDecisionType.valueOf(
                                decision.requiredString(
                                    key =
                                        "type"
                                )
                            ),
                        selectedServerKey =
                            decision.optionalString(
                                key =
                                    "selectedServerKey"
                            ),
                        confidence =
                            decision.requiredDouble(
                                key =
                                    "confidence"
                            ),
                        reason =
                            decision.requiredString(
                                key =
                                    "reason"
                            )
                    )
                }

        requireNoDuplicateIdentities(
            identities =
                decisions.map {
                    MatchIdentity(
                        catalogKey =
                            normalizeKey(
                                it.catalogKey
                            ),
                        serverArtifact =
                            it.serverArtifact
                    )
                },
            sourceName =
                "nutrition match decisions"
        )

        return decisions
    }

    private fun readValidationArtifact(
        file: File
    ): ValidationArtifact {

        val root =
            parseObject(
                file =
                    file
            )

        val version =
            root.requiredInt(
                key =
                    "version"
            )

        require(version == VALIDATION_VERSION) {
            "Unsupported representative nutrition validation version: " +
                    version
        }

        val entries =
            root.requiredArray(
                key =
                    "entries"
            )
                .map { element ->

                    require(element.isJsonObject) {
                        "Representative nutrition validation entry must " +
                                "be a JSON object."
                    }

                    element
                        .asJsonObject
                        .deepCopy()
                }

        return ValidationArtifact(
            version =
                version,
            entries =
                entries
        )
    }

    private fun writeValidationArtifact(
        version: Int,
        entries: List<JsonObject>,
        file: File
    ) {
        val root =
            JsonObject()

        root.addProperty(
            "version",
            version
        )

        val entriesJson =
            JsonArray()

        entries.forEach { entry ->

            entriesJson.add(
                entry
            )
        }

        root.add(
            "entries",
            entriesJson
        )

        writeJsonAtomically(
            root =
                root,
            file =
                file
        )
    }

    private fun writeJsonAtomically(
        root: JsonObject,
        file: File
    ) {
        val directory =
            requireNotNull(
                file.parentFile
            ) {
                "Representative validation file has no parent directory."
            }

        if (!directory.exists()) {
            check(directory.mkdirs()) {
                "Could not create representative validation directory: " +
                        directory.absolutePath
            }
        }

        require(directory.isDirectory) {
            "Representative validation parent is not a directory: " +
                    directory.absolutePath
        }

        val temporaryFile =
            File(
                directory,
                "${file.name}.tmp"
            )

        temporaryFile.writeText(
            GSON.toJson(
                root
            ) +
                    "\n"
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

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Expected JSON object in: " +
                    file.absolutePath
        }

        return root.asJsonObject
    }

    private fun requireNoDuplicateIdentities(
        identities: List<MatchIdentity>,
        sourceName: String
    ) {
        val duplicates =
            identities
                .groupingBy {
                    it
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicates.isEmpty()) {
            "$sourceName contain duplicate identities: " +
                    duplicates
                        .sortedWith(
                            compareBy<MatchIdentity>(
                                { it.serverArtifact },
                                { it.catalogKey }
                            )
                        )
                        .take(MAX_DIAGNOSTIC_KEYS)
                        .joinToString {
                            "${it.catalogKey} @ ${it.serverArtifact}"
                        }
        }
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

    private fun JsonObject.requiredInt(
        key: String
    ): Int {

        return get(key)
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asInt
            ?: error(
                "Missing integer value '$key'."
            )
    }

    private fun JsonObject.requiredBoolean(
        key: String
    ): Boolean {

        return get(key)
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isBoolean
            }
            ?.asBoolean
            ?: error(
                "Missing boolean value '$key'."
            )
    }

    private fun normalizeKey(
        value: String
    ): String {

        return value
            .trim()
            .lowercase(
                Locale.ROOT
            )
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

    private fun formatConfidence(
        value: Double
    ): String {

        return value
            .toBigDecimal()
            .stripTrailingZeros()
            .toPlainString()
    }

    private data class MatchIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )

    private data class PersistedRequest(
        val catalogKey: String,
        val serverArtifact: String,
        val candidates: List<PersistedCandidate>
    )

    private data class PersistedCandidate(
        val serverKey: String,
        val diagnosticScore: Double,
        val sharedTokens: List<String>
    )

    private data class PersistedDecision(
        val catalogKey: String,
        val serverArtifact: String,
        val type: CatalogKnowledgeMatchDecisionType,
        val selectedServerKey: String?,
        val confidence: Double,
        val reason: String
    )

    private data class ValidationArtifact(
        val version: Int,
        val entries: List<JsonObject>
    )

    private companion object {

        const val VALIDATION_VERSION =
            1

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        const val REJECTED_LOW_CONFIDENCE_STATUS =
            "REJECTED_LOW_CONFIDENCE"

        const val MAX_DIAGNOSTIC_KEYS =
            10

        val WHITESPACE_REGEX =
            Regex("\\s+")

        val GSON: Gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
    }
}

data class ProductiveLowConfidenceNutritionMatchValidationResult(
    val lowConfidenceMatchCount: Int,
    val existingValidationCount: Int,
    val newlyValidatedCount: Int,
    val newlyAcceptedCount: Int,
    val newlyRejectedCount: Int,
    val finalValidationEntryCount: Int,
    val validationFile: String
) {

    init {
        require(lowConfidenceMatchCount >= 0) {
            "lowConfidenceMatchCount must not be negative."
        }

        require(existingValidationCount >= 0) {
            "existingValidationCount must not be negative."
        }

        require(newlyValidatedCount >= 0) {
            "newlyValidatedCount must not be negative."
        }

        require(newlyAcceptedCount >= 0) {
            "newlyAcceptedCount must not be negative."
        }

        require(newlyRejectedCount >= 0) {
            "newlyRejectedCount must not be negative."
        }

        require(finalValidationEntryCount >= 0) {
            "finalValidationEntryCount must not be negative."
        }

        require(validationFile.isNotBlank()) {
            "validationFile must not be blank."
        }
    }
}