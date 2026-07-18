package de.shopme.tools.knowledge.report

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.PrintStream
import java.util.Locale

data class UnresolvedNutritionRetrievalFailureReport(
    val version: Int = 1,
    val thresholds: UnresolvedNutritionRetrievalFailureThresholds,
    val summary: UnresolvedNutritionRetrievalFailureSummary,
    val failures: List<UnresolvedNutritionRetrievalFailure>
)

data class UnresolvedNutritionRetrievalFailureThresholds(
    val highTopScoreMinimum: Double,
    val lowTopScoreMaximum: Double,
    val narrowScoreDeltaMaximum: Double,
    val largeScoreDeltaMinimum: Double,
    val shortCatalogKeyMaximumTokens: Int,
    val longCatalogKeyMinimumTokens: Int
)

data class UnresolvedNutritionRetrievalFailureSummary(
    val unresolvedCount: Int,
    val highTopScoreCount: Int,
    val lowTopScoreCount: Int,
    val mediumTopScoreCount: Int,
    val narrowScoreDeltaCount: Int,
    val largeScoreDeltaCount: Int,
    val mediumScoreDeltaCount: Int,
    val missingScoreDeltaCount: Int,
    val shortCatalogKeyCount: Int,
    val mediumCatalogKeyCount: Int,
    val longCatalogKeyCount: Int,
    val averageTopCandidateScore: Double?,
    val averageTopScoreDelta: Double?,
    val averageCandidateCount: Double,
    val averageCatalogTokenCount: Double,
    val decisionConfidenceDistribution: Map<String, Int>,
    val candidateCountDistribution: Map<Int, Int>,
    val catalogTokenCountDistribution: Map<Int, Int>
)

data class UnresolvedNutritionRetrievalFailure(
    val catalogKey: String,
    val validationStatus: String,
    val decisionType: String?,
    val decisionConfidence: Double?,
    val scoreBand: UnresolvedNutritionScoreBand,
    val scoreDeltaBand: UnresolvedNutritionScoreDeltaBand,
    val catalogKeyLengthBand: UnresolvedNutritionCatalogKeyLengthBand,
    val metrics: UnresolvedNutritionRetrievalMetrics,
    val candidates: List<UnresolvedNutritionRetrievalCandidate>
)

enum class UnresolvedNutritionScoreBand {

    HIGH,

    MEDIUM,

    LOW,

    MISSING
}

enum class UnresolvedNutritionScoreDeltaBand {

    NARROW,

    MEDIUM,

    LARGE,

    MISSING
}

enum class UnresolvedNutritionCatalogKeyLengthBand {

    SHORT,

    MEDIUM,

    LONG
}

data class UnresolvedNutritionRetrievalMetrics(
    val candidateCount: Int,
    val topCandidateScore: Double?,
    val secondCandidateScore: Double?,
    val topScoreDelta: Double?,
    val maximumSharedTokenCount: Int,
    val catalogTokenCount: Int,
    val topCandidateTokenCount: Int?,
    val topCandidateTokenRatio: Double?
)

data class UnresolvedNutritionRetrievalCandidate(
    val rank: Int,
    val serverKey: String,
    val diagnosticScore: Double,
    val sharedTokens: List<String>,
    val selected: Boolean
)

data class UnresolvedNutritionRetrievalFailureReportResult(
    val report: UnresolvedNutritionRetrievalFailureReport,
    val outputFile: File
)

/**
 * Erstellt einen kompakten Analysebericht für Retrieval-Fälle,
 * die der bestehende Failure Classifier als UNKNOWN eingestuft hat.
 *
 * Es werden keine bestehenden Artefakte verändert.
 */
class UnresolvedNutritionRetrievalFailureReporter(
    private val highTopScoreMinimum: Double = 0.75,
    private val lowTopScoreMaximum: Double = 0.55,
    private val narrowScoreDeltaMaximum: Double = 0.02,
    private val largeScoreDeltaMinimum: Double = 0.10,
    private val shortCatalogKeyMaximumTokens: Int = 2,
    private val longCatalogKeyMinimumTokens: Int = 5
) {

    init {
        require(highTopScoreMinimum in 0.0..1.0) {
            "highTopScoreMinimum must be between 0.0 and 1.0."
        }

        require(lowTopScoreMaximum in 0.0..1.0) {
            "lowTopScoreMaximum must be between 0.0 and 1.0."
        }

        require(lowTopScoreMaximum < highTopScoreMinimum) {
            "lowTopScoreMaximum must be below highTopScoreMinimum."
        }

        require(narrowScoreDeltaMaximum >= 0.0) {
            "narrowScoreDeltaMaximum must not be negative."
        }

        require(largeScoreDeltaMinimum > narrowScoreDeltaMaximum) {
            "largeScoreDeltaMinimum must exceed narrowScoreDeltaMaximum."
        }

        require(shortCatalogKeyMaximumTokens >= 1) {
            "shortCatalogKeyMaximumTokens must be at least 1."
        }

        require(
            longCatalogKeyMinimumTokens >
                    shortCatalogKeyMaximumTokens
        ) {
            "longCatalogKeyMinimumTokens must exceed " +
                    "shortCatalogKeyMaximumTokens."
        }
    }

    fun run(
        retrievalFailureFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): UnresolvedNutritionRetrievalFailureReportResult {

        require(retrievalFailureFile.isFile) {
            "Retrieval failure file does not exist: " +
                    retrievalFailureFile.absolutePath
        }

        val failures =
            readUnknownFailures(
                file = retrievalFailureFile
            )
                .map(::classify)
                .sortedBy {
                    it.catalogKey
                }

        val report =
            UnresolvedNutritionRetrievalFailureReport(
                thresholds =
                    UnresolvedNutritionRetrievalFailureThresholds(
                        highTopScoreMinimum =
                            highTopScoreMinimum,
                        lowTopScoreMaximum =
                            lowTopScoreMaximum,
                        narrowScoreDeltaMaximum =
                            narrowScoreDeltaMaximum,
                        largeScoreDeltaMinimum =
                            largeScoreDeltaMinimum,
                        shortCatalogKeyMaximumTokens =
                            shortCatalogKeyMaximumTokens,
                        longCatalogKeyMinimumTokens =
                            longCatalogKeyMinimumTokens
                    ),
                summary =
                    createSummary(
                        failures = failures
                    ),
                failures =
                    failures
            )

        writeReport(
            report = report,
            outputFile = outputFile
        )

        printReport(
            report = report,
            outputFile = outputFile,
            output = output
        )

        return UnresolvedNutritionRetrievalFailureReportResult(
            report = report,
            outputFile = outputFile
        )
    }

    private fun classify(
        source: SourceFailure
    ): UnresolvedNutritionRetrievalFailure {

        val scoreBand =
            when {
                source.metrics.topCandidateScore == null ->
                    UnresolvedNutritionScoreBand.MISSING

                source.metrics.topCandidateScore >=
                        highTopScoreMinimum ->
                    UnresolvedNutritionScoreBand.HIGH

                source.metrics.topCandidateScore <=
                        lowTopScoreMaximum ->
                    UnresolvedNutritionScoreBand.LOW

                else ->
                    UnresolvedNutritionScoreBand.MEDIUM
            }

        val scoreDeltaBand =
            when {
                source.metrics.topScoreDelta == null ->
                    UnresolvedNutritionScoreDeltaBand.MISSING

                source.metrics.topScoreDelta <=
                        narrowScoreDeltaMaximum ->
                    UnresolvedNutritionScoreDeltaBand.NARROW

                source.metrics.topScoreDelta >=
                        largeScoreDeltaMinimum ->
                    UnresolvedNutritionScoreDeltaBand.LARGE

                else ->
                    UnresolvedNutritionScoreDeltaBand.MEDIUM
            }

        val catalogKeyLengthBand =
            when {
                source.metrics.catalogTokenCount <=
                        shortCatalogKeyMaximumTokens ->
                    UnresolvedNutritionCatalogKeyLengthBand.SHORT

                source.metrics.catalogTokenCount >=
                        longCatalogKeyMinimumTokens ->
                    UnresolvedNutritionCatalogKeyLengthBand.LONG

                else ->
                    UnresolvedNutritionCatalogKeyLengthBand.MEDIUM
            }

        return UnresolvedNutritionRetrievalFailure(
            catalogKey =
                source.catalogKey,
            validationStatus =
                source.validationStatus,
            decisionType =
                source.decisionType,
            decisionConfidence =
                source.decisionConfidence,
            scoreBand =
                scoreBand,
            scoreDeltaBand =
                scoreDeltaBand,
            catalogKeyLengthBand =
                catalogKeyLengthBand,
            metrics =
                source.metrics,
            candidates =
                source.candidates
                    .sortedBy {
                        it.rank
                    }
        )
    }

    private fun createSummary(
        failures: List<UnresolvedNutritionRetrievalFailure>
    ): UnresolvedNutritionRetrievalFailureSummary {

        val topScores =
            failures.mapNotNull {
                it.metrics.topCandidateScore
            }

        val scoreDeltas =
            failures.mapNotNull {
                it.metrics.topScoreDelta
            }

        return UnresolvedNutritionRetrievalFailureSummary(
            unresolvedCount =
                failures.size,
            highTopScoreCount =
                failures.count {
                    it.scoreBand ==
                            UnresolvedNutritionScoreBand.HIGH
                },
            lowTopScoreCount =
                failures.count {
                    it.scoreBand ==
                            UnresolvedNutritionScoreBand.LOW
                },
            mediumTopScoreCount =
                failures.count {
                    it.scoreBand ==
                            UnresolvedNutritionScoreBand.MEDIUM
                },
            narrowScoreDeltaCount =
                failures.count {
                    it.scoreDeltaBand ==
                            UnresolvedNutritionScoreDeltaBand.NARROW
                },
            largeScoreDeltaCount =
                failures.count {
                    it.scoreDeltaBand ==
                            UnresolvedNutritionScoreDeltaBand.LARGE
                },
            mediumScoreDeltaCount =
                failures.count {
                    it.scoreDeltaBand ==
                            UnresolvedNutritionScoreDeltaBand.MEDIUM
                },
            missingScoreDeltaCount =
                failures.count {
                    it.scoreDeltaBand ==
                            UnresolvedNutritionScoreDeltaBand.MISSING
                },
            shortCatalogKeyCount =
                failures.count {
                    it.catalogKeyLengthBand ==
                            UnresolvedNutritionCatalogKeyLengthBand.SHORT
                },
            mediumCatalogKeyCount =
                failures.count {
                    it.catalogKeyLengthBand ==
                            UnresolvedNutritionCatalogKeyLengthBand.MEDIUM
                },
            longCatalogKeyCount =
                failures.count {
                    it.catalogKeyLengthBand ==
                            UnresolvedNutritionCatalogKeyLengthBand.LONG
                },
            averageTopCandidateScore =
                topScores.averageOrNull(),
            averageTopScoreDelta =
                scoreDeltas.averageOrNull(),
            averageCandidateCount =
                failures
                    .map {
                        it.metrics.candidateCount
                    }
                    .averageOrZero(),
            averageCatalogTokenCount =
                failures
                    .map {
                        it.metrics.catalogTokenCount
                    }
                    .averageOrZero(),
            decisionConfidenceDistribution =
                failures
                    .groupingBy {
                        confidenceBand(
                            confidence =
                                it.decisionConfidence
                        )
                    }
                    .eachCount()
                    .toSortedMap(),
            candidateCountDistribution =
                failures
                    .groupingBy {
                        it.metrics.candidateCount
                    }
                    .eachCount()
                    .toSortedMap(),
            catalogTokenCountDistribution =
                failures
                    .groupingBy {
                        it.metrics.catalogTokenCount
                    }
                    .eachCount()
                    .toSortedMap()
        )
    }

    private fun readUnknownFailures(
        file: File
    ): List<SourceFailure> {

        val root =
            parseObject(
                file = file
            )

        val version =
            root.requiredInt(
                key = "version"
            )

        require(version == INPUT_VERSION) {
            "Unsupported retrieval failure report version: $version"
        }

        val failures =
            root.requiredArray(
                key = "failures"
            )
                .mapNotNull { element ->

                    val failure =
                        element
                            .takeIf(JsonElement::isJsonObject)
                            ?.asJsonObject
                            ?: return@mapNotNull null

                    val primaryType =
                        failure.requiredString(
                            key = "primaryType"
                        )

                    if (
                        primaryType !=
                        PRIMARY_TYPE_UNKNOWN
                    ) {
                        return@mapNotNull null
                    }

                    val metrics =
                        failure.requiredObject(
                            key = "metrics"
                        )

                    SourceFailure(
                        catalogKey =
                            failure.requiredString(
                                key = "catalogKey"
                            ),
                        validationStatus =
                            failure.requiredString(
                                key = "validationStatus"
                            ),
                        decisionType =
                            failure.optionalString(
                                key = "decisionType"
                            ),
                        decisionConfidence =
                            failure.optionalDouble(
                                key = "decisionConfidence"
                            ),
                        metrics =
                            UnresolvedNutritionRetrievalMetrics(
                                candidateCount =
                                    metrics.requiredInt(
                                        key = "candidateCount"
                                    ),
                                topCandidateScore =
                                    metrics.optionalDouble(
                                        key = "topCandidateScore"
                                    ),
                                secondCandidateScore =
                                    metrics.optionalDouble(
                                        key = "secondCandidateScore"
                                    ),
                                topScoreDelta =
                                    metrics.optionalDouble(
                                        key = "topScoreDelta"
                                    ),
                                maximumSharedTokenCount =
                                    metrics.requiredInt(
                                        key = "maximumSharedTokenCount"
                                    ),
                                catalogTokenCount =
                                    metrics.requiredInt(
                                        key = "catalogTokenCount"
                                    ),
                                topCandidateTokenCount =
                                    metrics.optionalInt(
                                        key = "topCandidateTokenCount"
                                    ),
                                topCandidateTokenRatio =
                                    metrics.optionalDouble(
                                        key = "topCandidateTokenRatio"
                                    )
                            ),
                        candidates =
                            failure
                                .requiredArray(
                                    key = "candidates"
                                )
                                .map { candidateElement ->

                                    val candidate =
                                        candidateElement.asJsonObject

                                    UnresolvedNutritionRetrievalCandidate(
                                        rank =
                                            candidate.requiredInt(
                                                key = "rank"
                                            ),
                                        serverKey =
                                            candidate.requiredString(
                                                key = "serverKey"
                                            ),
                                        diagnosticScore =
                                            candidate.requiredDouble(
                                                key = "diagnosticScore"
                                            ),
                                        sharedTokens =
                                            candidate
                                                .requiredArray(
                                                    key = "sharedTokens"
                                                )
                                                .mapNotNull { token ->
                                                    token
                                                        .takeIf(
                                                            JsonElement::isJsonPrimitive
                                                        )
                                                        ?.asString
                                                        ?.trim()
                                                        ?.takeIf(
                                                            String::isNotBlank
                                                        )
                                                }
                                                .distinct()
                                                .sorted(),
                                        selected =
                                            candidate.requiredBoolean(
                                                key = "selected"
                                            )
                                    )
                                }
                    )
                }
                .sortedBy {
                    it.catalogKey
                }

        val duplicates =
            failures
                .groupingBy {
                    it.catalogKey
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys
                .sorted()

        require(duplicates.isEmpty()) {
            "Duplicate unresolved retrieval failure keys: $duplicates"
        }

        return failures
    }

    private fun writeReport(
        report: UnresolvedNutritionRetrievalFailureReport,
        outputFile: File
    ) {
        val parent =
            requireNotNull(
                outputFile.parentFile
            ) {
                "Output file has no parent directory: " +
                        outputFile.absolutePath
            }

        if (!parent.exists()) {
            check(parent.mkdirs()) {
                "Could not create output directory: " +
                        parent.absolutePath
            }
        }

        require(parent.isDirectory) {
            "Output parent is not a directory: " +
                    parent.absolutePath
        }

        val gson =
            GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()

        outputFile.writeText(
            gson.toJson(
                report
            )
        )
    }

    private fun printReport(
        report: UnresolvedNutritionRetrievalFailureReport,
        outputFile: File,
        output: PrintStream
    ) {
        val summary =
            report.summary

        output.println()
        output.println(SEPARATOR)
        output.println("UNRESOLVED NUTRITION RETRIEVAL FAILURES")
        output.println(SEPARATOR)

        output.println(
            "Unresolved catalog keys : " +
                    summary.unresolvedCount
        )

        output.println()
        output.println("Top candidate score:")

        output.println(
            "  HIGH   : " +
                    summary.highTopScoreCount
        )

        output.println(
            "  MEDIUM : " +
                    summary.mediumTopScoreCount
        )

        output.println(
            "  LOW    : " +
                    summary.lowTopScoreCount
        )

        output.println()
        output.println("Top score delta:")

        output.println(
            "  NARROW  : " +
                    summary.narrowScoreDeltaCount
        )

        output.println(
            "  MEDIUM  : " +
                    summary.mediumScoreDeltaCount
        )

        output.println(
            "  LARGE   : " +
                    summary.largeScoreDeltaCount
        )

        output.println(
            "  MISSING : " +
                    summary.missingScoreDeltaCount
        )

        output.println()
        output.println("Catalog key length:")

        output.println(
            "  SHORT  : " +
                    summary.shortCatalogKeyCount
        )

        output.println(
            "  MEDIUM : " +
                    summary.mediumCatalogKeyCount
        )

        output.println(
            "  LONG   : " +
                    summary.longCatalogKeyCount
        )

        output.println()

        output.println(
            "Average top score       : " +
                    summary.averageTopCandidateScore.formatNullable()
        )

        output.println(
            "Average top score delta : " +
                    summary.averageTopScoreDelta.formatNullable()
        )

        output.println(
            "Average candidates/key  : " +
                    summary.averageCandidateCount.format()
        )

        output.println(
            "Average catalog tokens  : " +
                    summary.averageCatalogTokenCount.format()
        )

        output.println()

        output.println(
            "Report written          : " +
                    outputFile.path
        )

        output.println(SEPARATOR)
    }

    private fun confidenceBand(
        confidence: Double?
    ): String =
        when {
            confidence == null ->
                "MISSING"

            confidence >= 0.90 ->
                "0.90-1.00"

            confidence >= 0.80 ->
                "0.80-0.89"

            confidence >= 0.70 ->
                "0.70-0.79"

            else ->
                "0.00-0.69"
        }

    private fun parseObject(
        file: File
    ): JsonObject {

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Expected JSON object in ${file.absolutePath}"
        }

        return root.asJsonObject
    }

    private fun JsonObject.requiredObject(
        key: String
    ): JsonObject =
        get(key)
            ?.takeIf(JsonElement::isJsonObject)
            ?.asJsonObject
            ?: error(
                "Missing object '$key'"
            )

    private fun JsonObject.requiredArray(
        key: String
    ): JsonArray =
        get(key)
            ?.takeIf(JsonElement::isJsonArray)
            ?.asJsonArray
            ?: error(
                "Missing array '$key'"
            )

    private fun JsonObject.requiredString(
        key: String
    ): String =
        optionalString(
            key = key
        )
            ?: error(
                "Missing or blank string '$key'"
            )

    private fun JsonObject.optionalString(
        key: String
    ): String? =
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive
            }
            ?.asString
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private fun JsonObject.requiredDouble(
        key: String
    ): Double =
        optionalDouble(
            key = key
        )
            ?: error(
                "Missing numeric '$key'"
            )

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

    private fun JsonObject.requiredInt(
        key: String
    ): Int =
        optionalInt(
            key = key
        )
            ?: error(
                "Missing integer '$key'"
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
            "Missing boolean '$key'"
        }

        return value.asBoolean
    }

    private fun List<Double>.averageOrNull(): Double? =
        if (isEmpty()) {
            null
        } else {
            average()
        }

    private fun List<Int>.averageOrZero(): Double =
        if (isEmpty()) {
            0.0
        } else {
            average()
        }

    private fun Double.format(): String =
        String.format(
            Locale.ROOT,
            "%.4f",
            this
        )

    private fun Double?.formatNullable(): String =
        this?.format()
            ?: "n/a"

    private data class SourceFailure(
        val catalogKey: String,
        val validationStatus: String,
        val decisionType: String?,
        val decisionConfidence: Double?,
        val metrics: UnresolvedNutritionRetrievalMetrics,
        val candidates:
        List<UnresolvedNutritionRetrievalCandidate>
    )

    private companion object {

        const val INPUT_VERSION =
            1

        const val PRIMARY_TYPE_UNKNOWN =
            "UNKNOWN"

        const val SEPARATOR =
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    }
}