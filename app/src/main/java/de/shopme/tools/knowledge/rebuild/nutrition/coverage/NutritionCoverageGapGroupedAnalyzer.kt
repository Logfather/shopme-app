package de.shopme.tools.knowledge.rebuild.nutrition.coverage

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class NutritionCoverageGapGroupedAnalyzer {

    fun analyze(
        coverageGapReportFile: File
    ): NutritionCoverageGapGroupedAnalysis {

        require(coverageGapReportFile.isFile) {
            "Nutrition coverage-gap report does not exist: " +
                    coverageGapReportFile.absolutePath
        }

        val root =
            parseObject(
                file =
                    coverageGapReportFile
            )

        val sourceVersion =
            root.requiredInt(
                key =
                    "version"
            )

        require(sourceVersion == SOURCE_REPORT_VERSION) {
            "Unsupported nutrition coverage-gap report version: " +
                    sourceVersion
        }

        val declaredMissingCount =
            root.requiredInt(
                key =
                    "missingCatalogItemCount"
            )

        val gaps =
            root.requiredArray(
                key =
                    "gaps"
            )
                .map { element ->

                    require(element.isJsonObject) {
                        "Nutrition coverage-gap entry must be a JSON object."
                    }

                    readGap(
                        value =
                            element.asJsonObject
                    )
                }

        require(gaps.size == declaredMissingCount) {
            "Coverage-gap report does not contain exactly the declared " +
                    "missing catalog items: declared=$declaredMissingCount, " +
                    "actual=${gaps.size}."
        }

        val groups =
            gaps
                .groupBy {
                    it.type
                }
                .map { (
                           type,
                           entries
                       ) ->

                    createGroup(
                        type =
                            type,
                        entries =
                            entries,
                        totalGapCount =
                            gaps.size
                    )
                }
                .sortedWith(
                    compareByDescending<NutritionCoverageGapTypeAnalysis> {
                        it.count
                    }
                        .thenBy {
                            it.type
                        }
                )

        return NutritionCoverageGapGroupedAnalysis(
            version =
                OUTPUT_REPORT_VERSION,
            totalGapCount =
                gaps.size,
            typeGroupCount =
                groups.size,
            groups =
                groups
        )
    }

    private fun createGroup(
        type: String,
        entries: List<GapEntry>,
        totalGapCount: Int
    ): NutritionCoverageGapTypeAnalysis {

        val sortedEntries =
            entries.sortedWith(
                compareByDescending<GapEntry> {
                    it.topCandidateScore
                        ?: Double.NEGATIVE_INFINITY
                }
                    .thenByDescending {
                        it.decisionConfidence
                            ?: Double.NEGATIVE_INFINITY
                    }
                    .thenBy {
                        it.catalogKey
                    }
            )

        return NutritionCoverageGapTypeAnalysis(
            type =
                type,
            count =
                entries.size,
            percentage =
                if (totalGapCount == 0) {
                    0.0
                } else {
                    entries.size.toDouble() /
                            totalGapCount.toDouble() *
                            100.0
                },
            requestExistsCount =
                entries.count {
                    it.requestExists
                },
            requestMissingCount =
                entries.count {
                    !it.requestExists
                },
            decisionExistsCount =
                entries.count {
                    it.decisionExists
                },
            decisionMissingCount =
                entries.count {
                    !it.decisionExists
                },
            mappingExistsCount =
                entries.count {
                    it.mappingExists
                },
            mappingMissingCount =
                entries.count {
                    !it.mappingExists
                },
            averageCandidateCount =
                entries
                    .map {
                        it.candidateCount.toDouble()
                    }
                    .averageOrNull(),
            averageDecisionConfidence =
                entries
                    .mapNotNull {
                        it.decisionConfidence
                    }
                    .averageOrNull(),
            averageTopCandidateScore =
                entries
                    .mapNotNull {
                        it.topCandidateScore
                    }
                    .averageOrNull(),
            averageTopScoreDelta =
                entries
                    .mapNotNull {
                        it.topScoreDelta
                    }
                    .averageOrNull(),
            decisionTypeCounts =
                countNullableStrings(
                    values =
                        entries.map {
                            it.decisionType
                        },
                    missingValue =
                        MISSING_DECISION_TYPE
                ),
            decisionSourceCounts =
                countNullableStrings(
                    values =
                        entries.map {
                            it.decisionSource
                        },
                    missingValue =
                        MISSING_DECISION_SOURCE
                ),
            examples =
                sortedEntries
                    .take(EXAMPLE_LIMIT)
                    .map {
                        NutritionCoverageGapTypeExample(
                            catalogKey =
                                it.catalogKey,
                            decisionType =
                                it.decisionType,
                            decisionSource =
                                it.decisionSource,
                            decisionConfidence =
                                it.decisionConfidence,
                            candidateCount =
                                it.candidateCount,
                            topCandidateKey =
                                it.topCandidateKey,
                            topCandidateScore =
                                it.topCandidateScore,
                            secondCandidateScore =
                                it.secondCandidateScore,
                            topScoreDelta =
                                it.topScoreDelta,
                            topCandidateSharedTokens =
                                it.topCandidateSharedTokens,
                            details =
                                it.details
                        )
                    }
        )
    }

    private fun readGap(
        value: JsonObject
    ): GapEntry {

        val catalogKey =
            value.requiredString(
                key =
                    "catalogKey"
            )

        val type =
            value.optionalString(
                key =
                    "type"
            )
                ?: UNKNOWN_TYPE

        val requestExists =
            value.requiredBoolean(
                key =
                    "requestExists"
            )

        val decisionExists =
            value.requiredBoolean(
                key =
                    "decisionExists"
            )

        val mappingExists =
            value.requiredBoolean(
                key =
                    "mappingExists"
            )

        val candidateCount =
            value.optionalInt(
                key =
                    "candidateCount"
            )
                ?: 0

        require(candidateCount >= 0) {
            "candidateCount must not be negative for '$catalogKey'."
        }

        return GapEntry(
            catalogKey =
                catalogKey,
            type =
                type,
            requestExists =
                requestExists,
            decisionExists =
                decisionExists,
            decisionType =
                value.optionalString(
                    key =
                        "decisionType"
                ),
            decisionSource =
                value.optionalString(
                    key =
                        "decisionSource"
                ),
            decisionConfidence =
                value.optionalDouble(
                    key =
                        "decisionConfidence"
                ),
            candidateCount =
                candidateCount,
            topCandidateKey =
                value.optionalString(
                    key =
                        "topCandidateKey"
                ),
            topCandidateScore =
                value.optionalDouble(
                    key =
                        "topCandidateScore"
                ),
            secondCandidateScore =
                value.optionalDouble(
                    key =
                        "secondCandidateScore"
                ),
            topScoreDelta =
                value.optionalDouble(
                    key =
                        "topScoreDelta"
                ),
            topCandidateSharedTokens =
                value.optionalStringArray(
                    key =
                        "topCandidateSharedTokens"
                ),
            mappingExists =
                mappingExists,
            details =
                value.optionalString(
                    key =
                        "details"
                )
                    ?: NO_DETAILS
        )
    }

    private fun countNullableStrings(
        values: List<String?>,
        missingValue: String
    ): Map<String, Int> =
        values
            .map {
                it ?: missingValue
            }
            .groupingBy {
                it
            }
            .eachCount()
            .toSortedMap()

    private fun List<Double>.averageOrNull():
            Double? =
        if (isEmpty()) {
            null
        } else {
            average()
        }

    private fun parseObject(
        file: File
    ): JsonObject {

        val parsed =
            JsonParser.parseString(
                file.readText()
            )

        require(parsed.isJsonObject) {
            "Expected JSON object in: " +
                    file.absolutePath
        }

        return parsed.asJsonObject
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
    ): String =
        optionalString(
            key =
                key
        )
            ?: error(
                "Missing or blank string '$key'."
            )

    private fun JsonObject.optionalString(
        key: String
    ): String? =
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

    private fun JsonObject.requiredInt(
        key: String
    ): Int =
        optionalInt(
            key =
                key
        )
            ?: error(
                "Missing integer '$key'."
            )

    private fun JsonObject.optionalInt(
        key: String
    ): Int? =
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asInt

    private fun JsonObject.optionalDouble(
        key: String
    ): Double? =
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asDouble

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

    private fun JsonObject.optionalStringArray(
        key: String
    ): List<String> {

        val value =
            get(key)
                ?: return emptyList()

        if (
            value.isJsonNull ||
            !value.isJsonArray
        ) {
            return emptyList()
        }

        return value
            .asJsonArray
            .mapNotNull { element ->

                element
                    .takeIf {
                        it.isJsonPrimitive &&
                                it.asJsonPrimitive.isString
                    }
                    ?.asString
                    ?.trim()
                    ?.takeIf {
                        it.isNotBlank()
                    }
            }
            .distinct()
            .sorted()
    }

    private data class GapEntry(
        val catalogKey: String,
        val type: String,
        val requestExists: Boolean,
        val decisionExists: Boolean,
        val decisionType: String?,
        val decisionSource: String?,
        val decisionConfidence: Double?,
        val candidateCount: Int,
        val topCandidateKey: String?,
        val topCandidateScore: Double?,
        val secondCandidateScore: Double?,
        val topScoreDelta: Double?,
        val topCandidateSharedTokens: List<String>,
        val mappingExists: Boolean,
        val details: String
    )

    private companion object {

        const val SOURCE_REPORT_VERSION =
            1

        const val OUTPUT_REPORT_VERSION =
            1

        const val EXAMPLE_LIMIT =
            10

        const val UNKNOWN_TYPE =
            "UNKNOWN"

        const val MISSING_DECISION_TYPE =
            "NONE"

        const val MISSING_DECISION_SOURCE =
            "NONE"

        const val NO_DETAILS =
            "No diagnostic details available."
    }
}