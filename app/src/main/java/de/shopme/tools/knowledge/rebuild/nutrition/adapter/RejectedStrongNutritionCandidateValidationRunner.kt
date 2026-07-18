package de.shopme.tools.knowledge.rebuild.nutrition.adapter

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class RejectedStrongNutritionCandidateValidationRunner(
    private val diagnosticReportFile: File,
    private val validator:
    RejectedStrongNutritionCandidateValidator =
        RejectedStrongNutritionCandidateValidator()
) {

    fun run():
            RejectedStrongNutritionCandidateValidationReport {

        require(diagnosticReportFile.isFile) {
            "Rejected strong nutrition candidate diagnostic report " +
                    "does not exist: " +
                    diagnosticReportFile.absolutePath
        }

        val root =
            parseObject(
                file =
                    diagnosticReportFile
            )

        val version =
            root.requiredInt(
                key =
                    "version"
            )

        require(version > 0) {
            "Unsupported rejected strong nutrition candidate " +
                    "diagnostic version: $version."
        }

        val diagnostics =
            root.requiredArray(
                key =
                    "diagnostics"
            )

        val reviewCandidates =
            diagnostics
                .map { element ->

                    require(element.isJsonObject) {
                        "Rejected strong nutrition diagnostic entry " +
                                "must be a JSON object."
                    }

                    readDiagnostic(
                        value =
                            element.asJsonObject
                    )
                }
                .filter {
                    it.representativeReviewRecommended
                }
                .map {
                    RejectedStrongNutritionCandidateValidationRequest(
                        catalogKey =
                            it.catalogKey,
                        selectedServerKey =
                            it.topCandidateKey,
                        diagnosticType =
                            it.diagnosticType,
                        originalNoMatchCause =
                            it.noMatchCause,
                        originalConfidence =
                            it.decisionConfidence,
                        candidateRank =
                            it.topCandidateRank,
                        diagnosticScore =
                            it.topCandidateScore,
                        sharedTokens =
                            it.topCandidateSharedTokens
                    )
                }
                .sortedBy {
                    normalizeKey(
                        value =
                            it.catalogKey
                    )
                }

        val duplicateCatalogKeys =
            reviewCandidates
                .groupingBy {
                    normalizeKey(
                        value =
                            it.catalogKey
                    )
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys

        require(duplicateCatalogKeys.isEmpty()) {
            "Diagnostic report contains duplicate representative " +
                    "review candidates: " +
                    duplicateCatalogKeys
                        .sorted()
                        .joinToString()
        }

        return validator.validate(
            candidates =
                reviewCandidates
        )
    }

    private fun readDiagnostic(
        value: JsonObject
    ): PersistedDiagnostic {

        val representativeReviewRecommended =
            value.requiredBoolean(
                key =
                    "representativeReviewRecommended"
            )

        val diagnostic =
            PersistedDiagnostic(
                catalogKey =
                    normalizeKey(
                        value.requiredString(
                            key =
                                "catalogKey"
                        )
                    ),
                noMatchCause =
                    value.requiredString(
                        key =
                            "noMatchCause"
                    ),
                topCandidateKey =
                    normalizeKey(
                        value.requiredString(
                            key =
                                "topCandidateKey"
                        )
                    ),
                topCandidateScore =
                    value.requiredDouble(
                        key =
                            "topCandidateScore"
                    ),
                topCandidateRank =
                    value.requiredInt(
                        key =
                            "topCandidateRank"
                    ),
                topCandidateSharedTokens =
                    value.requiredStringList(
                        key =
                            "topCandidateSharedTokens"
                    ),
                decisionConfidence =
                    value.requiredDouble(
                        key =
                            "decisionConfidence"
                    ),
                diagnosticType =
                    value.requiredString(
                        key =
                            "diagnosticType"
                    ),
                representativeReviewRecommended =
                    representativeReviewRecommended
            )

        require(
            diagnostic.topCandidateScore in
                    0.0..1.0
        ) {
            "topCandidateScore must be between 0.0 and 1.0 for " +
                    "'${diagnostic.catalogKey}'."
        }

        require(
            diagnostic.decisionConfidence in
                    0.0..1.0
        ) {
            "decisionConfidence must be between 0.0 and 1.0 for " +
                    "'${diagnostic.catalogKey}'."
        }

        require(
            diagnostic.topCandidateRank >=
                    1
        ) {
            "topCandidateRank must be at least 1 for " +
                    "'${diagnostic.catalogKey}'."
        }

        return diagnostic
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
                Regex("\\s+"),
                " "
            )
            .trim()

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
    ): String =
        get(key)
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
            ?: error(
                "Missing or blank string '$key'."
            )

    private fun JsonObject.requiredDouble(
        key: String
    ): Double =
        get(key)
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asDouble
            ?: error(
                "Missing numeric value '$key'."
            )

    private fun JsonObject.requiredInt(
        key: String
    ): Int =
        get(key)
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asInt
            ?: error(
                "Missing integer value '$key'."
            )

    private fun JsonObject.requiredBoolean(
        key: String
    ): Boolean =
        get(key)
            ?.takeIf {
                it.isJsonPrimitive &&
                        it.asJsonPrimitive.isBoolean
            }
            ?.asBoolean
            ?: error(
                "Missing boolean value '$key'."
            )

    private fun JsonObject.requiredStringList(
        key: String
    ): List<String> =
        requiredArray(
            key =
                key
        )
            .map { element ->

                require(
                    element.isJsonPrimitive &&
                            element.asJsonPrimitive.isString
                ) {
                    "'$key' must contain strings."
                }

                normalizeKey(
                    value =
                        element.asString
                )
            }
            .filter {
                it.isNotBlank()
            }
            .distinct()
            .sorted()

    private data class PersistedDiagnostic(
        val catalogKey: String,
        val noMatchCause: String,
        val topCandidateKey: String,
        val topCandidateScore: Double,
        val topCandidateRank: Int,
        val topCandidateSharedTokens: List<String>,
        val decisionConfidence: Double,
        val diagnosticType: String,
        val representativeReviewRecommended: Boolean
    )
}