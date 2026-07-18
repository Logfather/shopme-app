package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class PersistValidatedRejectedStrongNutritionMappings(
    private val representativeMappingMerger:
    RepresentativeNutritionMappingMerger =
        RepresentativeNutritionMappingMerger(),
    private val printLine: (String) -> Unit =
        ::println
) {

    fun run(
        validationFile: File,
        mappingFile: File
    ): PersistValidatedRejectedStrongNutritionMappingsResult {

        require(validationFile.isFile) {
            "Rejected strong nutrition candidate validation file " +
                    "does not exist: " +
                    validationFile.absolutePath
        }

        require(mappingFile.isFile) {
            "Catalog-server mapping file does not exist: " +
                    mappingFile.absolutePath
        }

        val validationSummary =
            readValidationSummary(
                file =
                    validationFile
            )

        val mergeResult =
            representativeMappingMerger.merge(
                representativeValidationFile =
                    validationFile,
                mappingFile =
                    mappingFile
            )

        require(
            mergeResult.representativeMappingCount ==
                    validationSummary.acceptedCount
        ) {
            "Representative merger accepted a different number of " +
                    "mappings than the validation report: " +
                    "report=${validationSummary.acceptedCount}, " +
                    "merger=${mergeResult.representativeMappingCount}."
        }

        require(
            mergeResult.representativeAddedCount +
                    mergeResult.representativeUnchangedCount ==
                    validationSummary.acceptedCount
        ) {
            "Representative merge outcomes do not cover all accepted " +
                    "validation entries: added=" +
                    mergeResult.representativeAddedCount +
                    ", unchanged=" +
                    mergeResult.representativeUnchangedCount +
                    ", accepted=" +
                    validationSummary.acceptedCount +
                    "."
        }

        val result =
            PersistValidatedRejectedStrongNutritionMappingsResult(
                validationEntryCount =
                    validationSummary.candidateCount,
                acceptedValidationCount =
                    validationSummary.acceptedCount,
                rejectedValidationCount =
                    validationSummary.rejectedCount,
                existingMappingCount =
                    mergeResult.existingMappingCount,
                addedMappingCount =
                    mergeResult.representativeAddedCount,
                unchangedMappingCount =
                    mergeResult.representativeUnchangedCount,
                finalMappingCount =
                    mergeResult.finalMappingCount,
                validationFile =
                    validationFile.path,
                mappingFile =
                    mappingFile.path
            )

        printSummary(
            result =
                result
        )

        return result
    }

    private fun readValidationSummary(
        file: File
    ): ValidationSummary {

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

        require(
            version ==
                    VALIDATION_VERSION
        ) {
            "Unsupported rejected strong nutrition validation " +
                    "version: $version."
        }

        val candidateCount =
            root.requiredInt(
                key =
                    "candidateCount"
            )

        val acceptedCount =
            root.requiredInt(
                key =
                    "acceptedCount"
            )

        val rejectedCount =
            root.requiredInt(
                key =
                    "rejectedCount"
            )

        val entries =
            root.requiredArray(
                key =
                    "entries"
            )

        require(candidateCount >= 0) {
            "candidateCount must not be negative."
        }

        require(acceptedCount >= 0) {
            "acceptedCount must not be negative."
        }

        require(rejectedCount >= 0) {
            "rejectedCount must not be negative."
        }

        require(
            candidateCount ==
                    entries.size()
        ) {
            "candidateCount differs from entries size: " +
                    "candidateCount=$candidateCount, " +
                    "entries=${entries.size()}."
        }

        require(
            acceptedCount +
                    rejectedCount ==
                    candidateCount
        ) {
            "Accepted and rejected counts do not cover all " +
                    "validation entries."
        }

        val persistedEntries =
            entries.map { element ->

                require(element.isJsonObject) {
                    "Rejected strong nutrition validation entry must " +
                            "be a JSON object."
                }

                readValidationEntry(
                    value =
                        element.asJsonObject
                )
            }

        require(
            persistedEntries.count {
                it.accepted
            } ==
                    acceptedCount
        ) {
            "acceptedCount differs from accepted entries."
        }

        require(
            persistedEntries.count {
                !it.accepted
            } ==
                    rejectedCount
        ) {
            "rejectedCount differs from rejected entries."
        }

        val duplicateCatalogKeys =
            persistedEntries
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Rejected strong nutrition validation contains duplicate " +
                    "catalog keys: " +
                    duplicateCatalogKeys
                        .sorted()
                        .joinToString()
        }

        require(
            persistedEntries ==
                    persistedEntries.sortedBy {
                        it.catalogKey
                    }
        ) {
            "Rejected strong nutrition validation entries must be " +
                    "sorted by catalogKey."
        }

        return ValidationSummary(
            candidateCount =
                candidateCount,
            acceptedCount =
                acceptedCount,
            rejectedCount =
                rejectedCount
        )
    }

    private fun readValidationEntry(
        value: JsonObject
    ): ValidationEntry {

        val catalogKey =
            normalizeKey(
                value.requiredString(
                    key =
                        "catalogKey"
                )
            )

        val selectedServerKey =
            normalizeKey(
                value.requiredString(
                    key =
                        "selectedServerKey"
                )
            )

        val decisionType =
            value.requiredString(
                key =
                    "decisionType"
            )

        val accepted =
            value.requiredBoolean(
                key =
                    "accepted"
            )

        require(
            decisionType in
                    SUPPORTED_DECISION_TYPES
        ) {
            "Unsupported rejected strong nutrition validation " +
                    "decisionType '$decisionType' for '$catalogKey'."
        }

        require(
            accepted ==
                    (
                            decisionType in
                                    ACCEPTED_DECISION_TYPES
                            )
        ) {
            "accepted does not correspond to decisionType for " +
                    "'$catalogKey': accepted=$accepted, " +
                    "decisionType='$decisionType'."
        }

        return ValidationEntry(
            catalogKey =
                catalogKey,
            selectedServerKey =
                selectedServerKey,
            decisionType =
                decisionType,
            accepted =
                accepted
        )
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

    private fun JsonObject.requiredArray(
        key: String
    ): JsonArray =
        get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
            ?: error(
                "Missing JSON array '$key'."
            )

    private fun JsonObject.requiredString(
        key: String
    ): String {

        val value =
            get(key)

        require(
            value != null &&
                    !value.isJsonNull &&
                    value.isJsonPrimitive &&
                    value.asJsonPrimitive.isString
        ) {
            "Missing string '$key'."
        }

        return value
            .asString
            .trim()
            .also {
                require(it.isNotBlank()) {
                    "String '$key' must not be blank."
                }
            }
    }

    private fun JsonObject.requiredInt(
        key: String
    ): Int {

        val value =
            get(key)

        require(
            value != null &&
                    !value.isJsonNull &&
                    value.isJsonPrimitive &&
                    value.asJsonPrimitive.isNumber
        ) {
            "Missing integer '$key'."
        }

        return value.asInt
    }

    private fun JsonObject.requiredBoolean(
        key: String
    ): Boolean {

        val value =
            get(key)

        require(
            value != null &&
                    !value.isJsonNull &&
                    value.isJsonPrimitive &&
                    value.asJsonPrimitive.isBoolean
        ) {
            "Missing boolean '$key'."
        }

        return value.asBoolean
    }

    private fun normalizeKey(
        value: String
    ): String =
        value
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

    private fun printSummary(
        result:
        PersistValidatedRejectedStrongNutritionMappingsResult
    ) {
        printLine("")
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        printLine(
            "REJECTED STRONG NUTRITION MAPPING PERSISTENCE"
        )
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        printLine(
            "Validation entries : " +
                    result.validationEntryCount
        )
        printLine(
            "Accepted           : " +
                    result.acceptedValidationCount
        )
        printLine(
            "Rejected           : " +
                    result.rejectedValidationCount
        )
        printLine(
            "Existing mappings  : " +
                    result.existingMappingCount
        )
        printLine(
            "Added mappings     : " +
                    result.addedMappingCount
        )
        printLine(
            "Unchanged mappings : " +
                    result.unchangedMappingCount
        )
        printLine(
            "Final mappings     : " +
                    result.finalMappingCount
        )
        printLine(
            "Validation file    : " +
                    result.validationFile
        )
        printLine(
            "Mapping file       : " +
                    result.mappingFile
        )
        printLine(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }

    private data class ValidationSummary(
        val candidateCount: Int,
        val acceptedCount: Int,
        val rejectedCount: Int
    )

    private data class ValidationEntry(
        val catalogKey: String,
        val selectedServerKey: String,
        val decisionType: String,
        val accepted: Boolean
    )

    private companion object {

        const val VALIDATION_VERSION =
            1

        val SUPPORTED_DECISION_TYPES =
            setOf(
                "IDENTICAL",
                "REPRESENTATIVE",
                "INCOMPATIBLE"
            )

        val ACCEPTED_DECISION_TYPES =
            setOf(
                "IDENTICAL",
                "REPRESENTATIVE"
            )

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}