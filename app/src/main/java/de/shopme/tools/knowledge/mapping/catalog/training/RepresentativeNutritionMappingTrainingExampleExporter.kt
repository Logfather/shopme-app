package de.shopme.tools.knowledge.mapping.catalog.training

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class RepresentativeNutritionMappingTrainingExampleExporter {

    fun run(
        validationFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): ExportRepresentativeNutritionMappingTrainingExamplesResult {

        require(validationFile.isFile) {
            "Representative nutrition validation file " +
                    "does not exist: " +
                    validationFile.absolutePath
        }

        val source =
            readValidationSource(
                file = validationFile
            )

        val examples =
            source.entries
                .asSequence()
                .filter {
                    it.accepted
                }
                .map {
                    toTrainingExample(
                        entry = it,
                        sourceFile = validationFile.name,
                        sourceVersion = source.version
                    )
                }
                .sortedWith(
                    compareBy<
                            RepresentativeNutritionMappingTrainingExample
                            >(
                        { it.catalogKey },
                        { it.serverKey },
                        { it.candidateRank },
                        { it.id }
                    )
                )
                .toList()

        validateUniqueExamples(
            examples = examples
        )

        val dataset =
            RepresentativeNutritionMappingTrainingDataset(
                summary =
                    RepresentativeNutritionMappingTrainingDatasetSummary(
                        sourceEntryCount =
                            source.entries.size,
                        exportedExampleCount =
                            examples.size,
                        identicalCount =
                            examples.count {
                                it.decisionType ==
                                        RepresentativeNutritionMappingDecisionType
                                            .IDENTICAL
                            },
                        representativeCount =
                            examples.count {
                                it.decisionType ==
                                        RepresentativeNutritionMappingDecisionType
                                            .REPRESENTATIVE
                            }
                    ),
                examples =
                    examples
            )

        writeDataset(
            dataset = dataset,
            outputFile = outputFile
        )

        printResult(
            dataset = dataset,
            outputFile = outputFile,
            output = output
        )

        return ExportRepresentativeNutritionMappingTrainingExamplesResult(
            dataset = dataset,
            outputFile = outputFile.absolutePath
        )
    }

    private fun readValidationSource(
        file: File
    ): ValidationSource {

        val root =
            JsonParser.parseString(
                file.readText()
            )
                .asJsonObject

        val version =
            root.requiredInt(
                key = "version",
                source = file.absolutePath
            )

        require(version == SUPPORTED_SOURCE_VERSION) {
            "Unsupported representative nutrition validation " +
                    "version: $version"
        }

        val entries =
            root.requiredArray(
                key = "entries",
                source = file.absolutePath
            )
                .map { element ->

                    val json =
                        element.asJsonObject

                    val decisionType =
                        parseDecisionType(
                            value =
                                json.requiredString(
                                    key = "decisionType",
                                    source =
                                        "representative nutrition " +
                                                "validation entry"
                                )
                        )

                    val accepted =
                        json.requiredBoolean(
                            key = "accepted",
                            source =
                                "representative nutrition " +
                                        "validation entry"
                        )

                    validateDecisionConsistency(
                        catalogKey =
                            json.requiredString(
                                key = "catalogKey",
                                source =
                                    "representative nutrition " +
                                            "validation entry"
                            ),
                        decisionType =
                            decisionType,
                        accepted =
                            accepted
                    )

                    ValidationEntry(
                        catalogKey =
                            normalizeKey(
                                json.requiredString(
                                    key = "catalogKey",
                                    source =
                                        "representative nutrition " +
                                                "validation entry"
                                )
                            ),
                        serverKey =
                            normalizeKey(
                                json.requiredString(
                                    key = "selectedServerKey",
                                    source =
                                        "representative nutrition " +
                                                "validation entry"
                                )
                            ),
                        candidateRank =
                            json.requiredInt(
                                key = "candidateRank",
                                source =
                                    "representative nutrition " +
                                            "validation entry"
                            ),
                        confidence =
                            json.requiredDouble(
                                key = "originalConfidence",
                                source =
                                    "representative nutrition " +
                                            "validation entry"
                            ),
                        originalDecisionReason =
                            json.optionalString(
                                key = "originalDecisionReason"
                            ),
                        originalValidationStatus =
                            json.requiredString(
                                key = "originalValidationStatus",
                                source =
                                    "representative nutrition " +
                                            "validation entry"
                            ),
                        originalValidationReason =
                            json.optionalString(
                                key = "originalValidationReason"
                            ),
                        decisionType =
                            decisionType,
                        reasons =
                            json.requiredStringArray(
                                key = "reasons",
                                source =
                                    "representative nutrition " +
                                            "validation entry"
                            ),
                        accepted =
                            accepted
                    )
                }

        return ValidationSource(
            version = version,
            entries = entries
        )
    }

    private fun toTrainingExample(
        entry: ValidationEntry,
        sourceFile: String,
        sourceVersion: Int
    ): RepresentativeNutritionMappingTrainingExample {

        require(entry.accepted) {
            "Only accepted representative nutrition mappings " +
                    "may be exported."
        }

        require(
            entry.decisionType ==
                    RepresentativeNutritionMappingDecisionType.IDENTICAL ||
                    entry.decisionType ==
                    RepresentativeNutritionMappingDecisionType.REPRESENTATIVE
        ) {
            "Accepted training example must be IDENTICAL or " +
                    "REPRESENTATIVE: " +
                    "${entry.catalogKey} -> ${entry.serverKey}"
        }

        require(entry.candidateRank > 0) {
            "Candidate rank must be greater than zero: " +
                    "${entry.catalogKey} -> ${entry.serverKey}"
        }

        require(entry.confidence in 0.0..1.0) {
            "Confidence must be between 0.0 and 1.0: " +
                    "${entry.catalogKey} -> ${entry.serverKey}"
        }

        return RepresentativeNutritionMappingTrainingExample(
            id =
                createStableId(
                    catalogKey =
                        entry.catalogKey,
                    serverKey =
                        entry.serverKey,
                    candidateRank =
                        entry.candidateRank
                ),
            catalogKey =
                entry.catalogKey,
            serverArtifact =
                NUTRITION_ARTIFACT,
            serverKey =
                entry.serverKey,
            candidateRank =
                entry.candidateRank,
            confidence =
                entry.confidence,
            decisionType =
                entry.decisionType,
            accepted =
                true,
            reasons =
                entry.reasons
                    .map {
                        it.trim()
                    }
                    .filter {
                        it.isNotBlank()
                    }
                    .distinct()
                    .sorted(),
            originalDecisionReason =
                entry.originalDecisionReason,
            originalValidationStatus =
                entry.originalValidationStatus,
            originalValidationReason =
                entry.originalValidationReason,
            provenance =
                RepresentativeNutritionMappingTrainingProvenance(
                    sourceType =
                        SOURCE_TYPE,
                    sourceFile =
                        sourceFile,
                    sourceVersion =
                        sourceVersion,
                    matcher =
                        MATCHER,
                    validator =
                        VALIDATOR
                )
        )
    }

    private fun validateDecisionConsistency(
        catalogKey: String,
        decisionType:
        RepresentativeNutritionMappingDecisionType,
        accepted: Boolean
    ) {
        val expectedAccepted =
            decisionType ==
                    RepresentativeNutritionMappingDecisionType.IDENTICAL ||
                    decisionType ==
                    RepresentativeNutritionMappingDecisionType.REPRESENTATIVE

        require(accepted == expectedAccepted) {
            "Inconsistent representative nutrition validation " +
                    "decision for '$catalogKey': " +
                    "decisionType=$decisionType, accepted=$accepted"
        }
    }

    private fun validateUniqueExamples(
        examples:
        List<RepresentativeNutritionMappingTrainingExample>
    ) {
        val duplicateIds =
            examples
                .groupingBy {
                    it.id
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateIds.isEmpty()) {
            "Duplicate representative nutrition training example " +
                    "IDs: " +
                    duplicateIds
                        .sorted()
                        .joinToString()
        }

        val duplicateMappings =
            examples
                .groupingBy {
                    MappingIdentity(
                        catalogKey =
                            it.catalogKey,
                        serverKey =
                            it.serverKey
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateMappings.isEmpty()) {
            "Duplicate representative nutrition training " +
                    "mappings: " +
                    duplicateMappings
                        .sortedWith(
                            compareBy<MappingIdentity>(
                                { it.catalogKey },
                                { it.serverKey }
                            )
                        )
                        .joinToString {
                            "${it.catalogKey} -> ${it.serverKey}"
                        }
        }
    }

    private fun createStableId(
        catalogKey: String,
        serverKey: String,
        candidateRank: Int
    ): String {

        val canonicalValue =
            listOf(
                DATASET_NAMESPACE,
                catalogKey,
                NUTRITION_ARTIFACT,
                serverKey,
                candidateRank.toString()
            )
                .joinToString(
                    separator = "|"
                )

        val digest =
            MessageDigest
                .getInstance("SHA-256")
                .digest(
                    canonicalValue.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )

        return digest
            .joinToString(
                separator = ""
            ) {
                "%02x".format(
                    it.toInt() and 0xff
                )
            }
    }

    private fun writeDataset(
        dataset:
        RepresentativeNutritionMappingTrainingDataset,
        outputFile: File
    ) {
        outputFile.parentFile
            ?.let { directory ->

                if (!directory.exists()) {
                    check(directory.mkdirs()) {
                        "Could not create training dataset " +
                                "directory: " +
                                directory.absolutePath
                    }
                }

                require(directory.isDirectory) {
                    "Training dataset parent path is not a " +
                            "directory: " +
                            directory.absolutePath
                }
            }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        outputFile.writeText(
            gson.toJson(dataset) + "\n"
        )
    }

    private fun printResult(
        dataset:
        RepresentativeNutritionMappingTrainingDataset,
        outputFile: File,
        output: PrintStream
    ) {
        val summary =
            dataset.summary

        output.println()
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "REPRESENTATIVE NUTRITION TRAINING EXAMPLES"
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
        output.println(
            "Source entries           : " +
                    summary.sourceEntryCount
        )
        output.println(
            "Exported examples        : " +
                    summary.exportedExampleCount
        )
        output.println(
            "IDENTICAL                : " +
                    summary.identicalCount
        )
        output.println(
            "REPRESENTATIVE           : " +
                    summary.representativeCount
        )
        output.println()
        output.println(
            "Output                    : " +
                    outputFile.absolutePath
        )
        output.println(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        )
    }

    private fun parseDecisionType(
        value: String
    ): RepresentativeNutritionMappingDecisionType {

        return runCatching {
            RepresentativeNutritionMappingDecisionType.valueOf(
                value
            )
        }
            .getOrElse {
                error(
                    "Unknown representative nutrition decision " +
                            "type: $value"
                )
            }
    }

    private fun JsonObject.requiredArray(
        key: String,
        source: String
    ): JsonArray {

        return get(key)
            ?.takeIf {
                it.isJsonArray
            }
            ?.asJsonArray
            ?: error(
                "Missing array '$key' in $source."
            )
    }

    private fun JsonObject.requiredStringArray(
        key: String,
        source: String
    ): List<String> {

        return requiredArray(
            key = key,
            source = source
        )
            .mapIndexed { index, element ->

                require(
                    element.isJsonPrimitive &&
                            element.asJsonPrimitive.isString
                ) {
                    "Expected string at '$key[$index]' in $source."
                }

                element.asString
                    .trim()
                    .also {
                        require(it.isNotBlank()) {
                            "Blank string at '$key[$index]' " +
                                    "in $source."
                        }
                    }
            }
            .distinct()
            .sorted()
    }

    private fun JsonObject.requiredString(
        key: String,
        source: String
    ): String {

        return optionalString(
            key = key
        )
            ?: error(
                "Missing or blank string '$key' in $source."
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
        key: String,
        source: String
    ): Int {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing integer '$key' in $source."
        }

        return element.asInt
    }

    private fun JsonObject.requiredDouble(
        key: String,
        source: String
    ): Double {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing number '$key' in $source."
        }

        return element.asDouble
    }

    private fun JsonObject.requiredBoolean(
        key: String,
        source: String
    ): Boolean {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isBoolean
        ) {
            "Missing boolean '$key' in $source."
        }

        return element.asBoolean
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

    private data class ValidationSource(
        val version: Int,
        val entries: List<ValidationEntry>
    )

    private data class ValidationEntry(
        val catalogKey: String,
        val serverKey: String,
        val candidateRank: Int,
        val confidence: Double,
        val originalDecisionReason: String?,
        val originalValidationStatus: String,
        val originalValidationReason: String?,
        val decisionType:
        RepresentativeNutritionMappingDecisionType,
        val reasons: List<String>,
        val accepted: Boolean
    )

    private data class MappingIdentity(
        val catalogKey: String,
        val serverKey: String
    )

    private companion object {

        const val SUPPORTED_SOURCE_VERSION =
            1

        const val NUTRITION_ARTIFACT =
            "nutrition.json"

        const val DATASET_NAMESPACE =
            "representative-nutrition-mapping-v1"

        const val SOURCE_TYPE =
            "GPT_5_5_LOW_CONFIDENCE_REVALIDATION"

        const val MATCHER =
            "GPT-5.5 CatalogToServerMatcher"

        const val VALIDATOR =
            "DeterministicRepresentativeNutritionMappingValidator"

        val WHITESPACE_REGEX =
            Regex("\\s+")
    }
}