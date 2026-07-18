package de.shopme.tools.knowledge.mapping.catalog.representative

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.PrintStream

class PersistRepresentativeNutritionMappings {

    fun run(
        validationFile: File,
        mappingFile: File,
        output: PrintStream = System.out
    ): PersistRepresentativeNutritionMappingsResult {

        require(validationFile.isFile) {
            "Representative nutrition validation file does not exist: " +
                    validationFile.absolutePath
        }

        val validations =
            readAcceptedValidations(
                file = validationFile
            )

        val existingMappings =
            readMappings(
                file = mappingFile
            )

        val mergedMappings =
            existingMappings
                .toMutableMap()

        var addedMappingCount =
            0

        var unchangedMappingCount =
            0

        validations.forEach { validation ->

            val identity =
                MappingIdentity(
                    catalogKey =
                        validation.catalogKey,
                    serverArtifact =
                        NUTRITION_ARTIFACT
                )

            val existing =
                mergedMappings[identity]

            when {

                existing == null -> {

                    mergedMappings[identity] =
                        PersistedMapping(
                            catalogKey =
                                validation.catalogKey,
                            serverArtifact =
                                NUTRITION_ARTIFACT,
                            serverKey =
                                validation.serverKey
                        )

                    addedMappingCount++
                }

                existing.serverKey ==
                        validation.serverKey -> {

                    unchangedMappingCount++
                }

                else -> {

                    error(
                        "Conflicting representative nutrition mapping: " +
                                "${validation.catalogKey} is already mapped " +
                                "to '${existing.serverKey}', but the " +
                                "representative validation selected " +
                                "'${validation.serverKey}'."
                    )
                }
            }
        }

        val sortedMappings =
            mergedMappings
                .values
                .sortedWith(
                    compareBy<PersistedMapping>(
                        { it.catalogKey },
                        { it.serverArtifact },
                        { it.serverKey }
                    )
                )

        writeMappings(
            mappings = sortedMappings,
            file = mappingFile
        )

        val result =
            PersistRepresentativeNutritionMappingsResult(
                validationEntryCount =
                    readValidationEntryCount(
                        file = validationFile
                    ),
                acceptedValidationCount =
                    validations.size,
                existingMappingCount =
                    existingMappings.size,
                addedMappingCount =
                    addedMappingCount,
                unchangedMappingCount =
                    unchangedMappingCount,
                finalMappingCount =
                    sortedMappings.size,
                outputMappingFile =
                    mappingFile.absolutePath
            )

        printResult(
            result = result,
            output = output
        )

        return result
    }

    private fun readAcceptedValidations(
        file: File
    ): List<AcceptedValidation> {

        val root =
            parseObject(
                file = file
            )

        val version =
            root.requiredInt(
                key = "version"
            )

        require(version == VALIDATION_VERSION) {
            "Unsupported representative nutrition validation version: " +
                    version
        }

        val entries =
            root.requiredArray(
                key = "entries"
            )

        val accepted =
            entries
                .map { element ->

                    val entry =
                        element.asJsonObject

                    ValidationEntry(
                        catalogKey =
                            normalizeKey(
                                entry.requiredString(
                                    key = "catalogKey"
                                )
                            ),
                        serverKey =
                            entry.requiredString(
                                key = "selectedServerKey"
                            ),
                        decisionType =
                            RepresentativeNutritionMappingDecisionType
                                .valueOf(
                                    entry.requiredString(
                                        key = "decisionType"
                                    )
                                ),
                        accepted =
                            entry.requiredBoolean(
                                key = "accepted"
                            )
                    )
                }
                .filter {
                    it.accepted
                }
                .map { entry ->

                    require(
                        entry.decisionType ==
                                RepresentativeNutritionMappingDecisionType
                                    .IDENTICAL ||
                                entry.decisionType ==
                                RepresentativeNutritionMappingDecisionType
                                    .REPRESENTATIVE
                    ) {
                        "Accepted representative nutrition validation " +
                                "must be IDENTICAL or REPRESENTATIVE: " +
                                "${entry.catalogKey} -> " +
                                "${entry.serverKey} " +
                                "(${entry.decisionType})"
                    }

                    AcceptedValidation(
                        catalogKey =
                            entry.catalogKey,
                        serverKey =
                            entry.serverKey
                    )
                }
                .sortedWith(
                    compareBy<AcceptedValidation>(
                        { it.catalogKey },
                        { it.serverKey }
                    )
                )

        val duplicateCatalogKeys =
            accepted
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Duplicate accepted representative nutrition mappings: " +
                    duplicateCatalogKeys
                        .sorted()
                        .joinToString()
        }

        return accepted
    }

    private fun readValidationEntryCount(
        file: File
    ): Int {

        return parseObject(
            file = file
        )
            .requiredArray(
                key = "entries"
            )
            .size()
    }

    private fun readMappings(
        file: File
    ): Map<MappingIdentity, PersistedMapping> {

        if (!file.isFile) {
            return emptyMap()
        }

        val root =
            parseObject(
                file = file
            )

        val mappings =
            root["mappings"]
                ?.takeIf {
                    it.isJsonArray
                }
                ?.asJsonArray
                ?: return emptyMap()

        val parsed =
            mappings.map { element ->

                val mapping =
                    element.asJsonObject

                val catalogKey =
                    normalizeKey(
                        mapping.requiredString(
                            key = "catalogKey"
                        )
                    )

                val serverArtifact =
                    mapping.optionalString(
                        key = "serverArtifact"
                    )
                        ?: mapping.optionalString(
                            key = "sourceArtifact"
                        )
                        ?: NUTRITION_ARTIFACT

                val serverKey =
                    mapping.requiredString(
                        key = "serverKey"
                    )

                PersistedMapping(
                    catalogKey =
                        catalogKey,
                    serverArtifact =
                        serverArtifact,
                    serverKey =
                        serverKey
                )
            }

        val duplicateIdentities =
            parsed
                .groupingBy {
                    MappingIdentity(
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
            "Duplicate catalog-server mapping identities: " +
                    duplicateIdentities
                        .sortedWith(
                            compareBy<MappingIdentity>(
                                { it.catalogKey },
                                { it.serverArtifact }
                            )
                        )
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverArtifact}"
                        }
        }

        return parsed.associateBy { mapping ->

            MappingIdentity(
                catalogKey =
                    mapping.catalogKey,
                serverArtifact =
                    mapping.serverArtifact
            )
        }
    }

    private fun writeMappings(
        mappings: List<PersistedMapping>,
        file: File
    ) {
        file.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create mapping directory: " +
                                directory.absolutePath
                    }
                }

                require(directory.isDirectory) {
                    "Mapping parent path is not a directory: " +
                            directory.absolutePath
                }
            }

        val root =
            JsonObject()

        root.addProperty(
            "version",
            MAPPING_VERSION
        )

        val mappingsJson =
            JsonArray()

        mappings.forEach { mapping ->

            val mappingJson =
                JsonObject()

            mappingJson.addProperty(
                "catalogKey",
                mapping.catalogKey
            )

            mappingJson.addProperty(
                "serverArtifact",
                mapping.serverArtifact
            )

            mappingJson.addProperty(
                "serverKey",
                mapping.serverKey
            )

            mappingsJson.add(
                mappingJson
            )
        }

        root.add(
            "mappings",
            mappingsJson
        )

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        file.writeText(
            gson.toJson(root) + "\n"
        )
    }

    private fun printResult(
        result: PersistRepresentativeNutritionMappingsResult,
        output: PrintStream
    ) {
        output.println()
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "PERSIST REPRESENTATIVE NUTRITION MAPPINGS"
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "Validation entries       : " +
                    result.validationEntryCount
        )
        output.println(
            "Accepted validations     : " +
                    result.acceptedValidationCount
        )
        output.println(
            "Existing mappings        : " +
                    result.existingMappingCount
        )
        output.println(
            "Added mappings           : " +
                    result.addedMappingCount
        )
        output.println(
            "Unchanged mappings       : " +
                    result.unchangedMappingCount
        )
        output.println(
            "Final mappings           : " +
                    result.finalMappingCount
        )
        output.println()
        output.println(
            "Mappings written         : " +
                    result.outputMappingFile
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
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

        return get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
            ?: error(
                "Missing array '$key'."
            )
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
                        it.isJsonPrimitive
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
    ): String {

        return value
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .replace(
                WHITESPACE_REGEX,
                " "
            )
            .trim()
    }

    private data class ValidationEntry(
        val catalogKey: String,
        val serverKey: String,
        val decisionType:
        RepresentativeNutritionMappingDecisionType,
        val accepted: Boolean
    )

    private data class AcceptedValidation(
        val catalogKey: String,
        val serverKey: String
    )

    private data class PersistedMapping(
        val catalogKey: String,
        val serverArtifact: String,
        val serverKey: String
    )

    private data class MappingIdentity(
        val catalogKey: String,
        val serverArtifact: String
    )

    private companion object {

        const val VALIDATION_VERSION =
            1

        const val MAPPING_VERSION =
            1

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}