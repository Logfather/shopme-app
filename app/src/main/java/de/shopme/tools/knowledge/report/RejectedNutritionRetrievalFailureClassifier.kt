package de.shopme.tools.knowledge.report

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.PrintStream
import java.util.Locale

enum class RejectedNutritionRetrievalFailureType {

    NO_CANDIDATES,

    SELECTED_NOT_TOP_RANKED,

    NO_SHARED_TOKENS,

    VERY_LOW_SCORE,

    WEAK_TOKEN_OVERLAP,

    SCORE_CLUSTER,

    PROCESSING_STATE_MISMATCH,

    PRODUCT_FORM_MISMATCH,

    MODIFIER_MISMATCH,

    TOO_GENERIC,

    TOO_SPECIFIC,

    UNKNOWN
}

data class RejectedNutritionRetrievalFailureThresholds(
    val veryLowScore: Double,
    val scoreClusterMaximumDelta: Double,
    val weakSharedTokenMaximum: Int,
    val tooGenericTokenRatio: Double,
    val tooSpecificTokenRatio: Double
)

data class RejectedNutritionRetrievalFailureMetrics(
    val candidateCount: Int,
    val topCandidateScore: Double?,
    val secondCandidateScore: Double?,
    val topScoreDelta: Double?,
    val maximumSharedTokenCount: Int,
    val catalogTokenCount: Int,
    val topCandidateTokenCount: Int?,
    val topCandidateTokenRatio: Double?
)

data class RejectedNutritionRetrievalFailureCandidate(
    val rank: Int,
    val serverKey: String,
    val diagnosticScore: Double,
    val sharedTokens: List<String>,
    val selected: Boolean
)

data class RejectedNutritionRetrievalFailure(
    val catalogKey: String,
    val validationStatus: String,
    val decisionType: String?,
    val decisionConfidence: Double?,
    val selectedServerKey: String?,
    val selectedCandidateRank: Int?,
    val primaryType: RejectedNutritionRetrievalFailureType,
    val signals: List<RejectedNutritionRetrievalFailureType>,
    val metrics: RejectedNutritionRetrievalFailureMetrics,
    val candidates: List<RejectedNutritionRetrievalFailureCandidate>
)

data class RejectedNutritionRetrievalFailureSummary(
    val rejectedCatalogKeyCount: Int,
    val noMatchCount: Int,
    val lowConfidenceCount: Int,
    val unknownCount: Int,
    val primaryTypeCounts:
    Map<RejectedNutritionRetrievalFailureType, Int>,
    val signalCounts:
    Map<RejectedNutritionRetrievalFailureType, Int>
)

data class RejectedNutritionRetrievalFailureReport(
    val version: Int = 1,
    val thresholds: RejectedNutritionRetrievalFailureThresholds,
    val summary: RejectedNutritionRetrievalFailureSummary,
    val failures: List<RejectedNutritionRetrievalFailure>
)

data class RejectedNutritionRetrievalFailureReportResult(
    val report: RejectedNutritionRetrievalFailureReport,
    val outputFile: File
)

class RejectedNutritionRetrievalFailureClassifier(
    private val veryLowScoreThreshold: Double = 0.40,
    private val scoreClusterMaximumDelta: Double = 0.02,
    private val weakSharedTokenMaximum: Int = 1,
    private val tooGenericTokenRatio: Double = 0.50,
    private val tooSpecificTokenRatio: Double = 2.00
) {

    init {
        require(veryLowScoreThreshold in 0.0..1.0) {
            "veryLowScoreThreshold must be between 0.0 and 1.0"
        }

        require(scoreClusterMaximumDelta >= 0.0) {
            "scoreClusterMaximumDelta must not be negative"
        }

        require(weakSharedTokenMaximum >= 0) {
            "weakSharedTokenMaximum must not be negative"
        }

        require(tooGenericTokenRatio in 0.0..1.0) {
            "tooGenericTokenRatio must be between 0.0 and 1.0"
        }

        require(tooSpecificTokenRatio >= 1.0) {
            "tooSpecificTokenRatio must be at least 1.0"
        }
    }

    fun run(
        candidateQualityFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): RejectedNutritionRetrievalFailureReportResult {

        require(candidateQualityFile.isFile) {
            "Rejected candidate quality file does not exist: " +
                    candidateQualityFile.absolutePath
        }

        val entries =
            readEntries(
                file = candidateQualityFile
            )

        val failures =
            entries
                .map(::classify)
                .sortedBy {
                    it.catalogKey
                }

        val report =
            RejectedNutritionRetrievalFailureReport(
                thresholds =
                    RejectedNutritionRetrievalFailureThresholds(
                        veryLowScore =
                            veryLowScoreThreshold,
                        scoreClusterMaximumDelta =
                            scoreClusterMaximumDelta,
                        weakSharedTokenMaximum =
                            weakSharedTokenMaximum,
                        tooGenericTokenRatio =
                            tooGenericTokenRatio,
                        tooSpecificTokenRatio =
                            tooSpecificTokenRatio
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

        return RejectedNutritionRetrievalFailureReportResult(
            report = report,
            outputFile = outputFile
        )
    }

    private fun classify(
        entry: SourceEntry
    ): RejectedNutritionRetrievalFailure {

        val catalogTokens =
            tokenize(
                value = entry.catalogKey
            )

        val candidates =
            entry.candidates
                .sortedBy {
                    it.rank
                }
                .map { candidate ->
                    RejectedNutritionRetrievalFailureCandidate(
                        rank =
                            candidate.rank,
                        serverKey =
                            candidate.serverKey,
                        diagnosticScore =
                            candidate.diagnosticScore,
                        sharedTokens =
                            candidate.sharedTokens
                                .map(::normalize)
                                .filter(String::isNotBlank)
                                .distinct()
                                .sorted(),
                        selected =
                            candidate.selected
                    )
                }

        val topCandidate =
            candidates.firstOrNull()

        val secondCandidate =
            candidates.getOrNull(1)

        val topCandidateTokens =
            topCandidate
                ?.serverKey
                ?.let(::tokenize)
                .orEmpty()

        val topScoreDelta =
            if (
                topCandidate != null &&
                secondCandidate != null
            ) {
                topCandidate.diagnosticScore -
                        secondCandidate.diagnosticScore
            } else {
                null
            }

        val maximumSharedTokenCount =
            candidates
                .maxOfOrNull {
                    it.sharedTokens.size
                }
                ?: 0

        val tokenRatio =
            if (
                topCandidate != null &&
                catalogTokens.isNotEmpty()
            ) {
                topCandidateTokens.size.toDouble() /
                        catalogTokens.size.toDouble()
            } else {
                null
            }

        val signals =
            linkedSetOf<RejectedNutritionRetrievalFailureType>()

        if (candidates.isEmpty()) {
            signals +=
                RejectedNutritionRetrievalFailureType.NO_CANDIDATES
        }

        if (
            entry.selectedCandidateRank != null &&
            entry.selectedCandidateRank > 1
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType
                    .SELECTED_NOT_TOP_RANKED
        }

        if (
            candidates.isNotEmpty() &&
            maximumSharedTokenCount == 0
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType.NO_SHARED_TOKENS
        }

        if (
            topCandidate != null &&
            topCandidate.diagnosticScore <
            veryLowScoreThreshold
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType.VERY_LOW_SCORE
        }

        if (
            candidates.isNotEmpty() &&
            maximumSharedTokenCount in
            1..weakSharedTokenMaximum
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType.WEAK_TOKEN_OVERLAP
        }

        if (
            topScoreDelta != null &&
            topScoreDelta >= 0.0 &&
            topScoreDelta <= scoreClusterMaximumDelta
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType.SCORE_CLUSTER
        }

        if (
            topCandidate != null &&
            hasProcessingStateMismatch(
                catalogTokens = catalogTokens,
                candidateTokens = topCandidateTokens
            )
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType
                    .PROCESSING_STATE_MISMATCH
        }

        if (
            topCandidate != null &&
            hasProductFormMismatch(
                catalogTokens = catalogTokens,
                candidateTokens = topCandidateTokens
            )
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType
                    .PRODUCT_FORM_MISMATCH
        }

        if (
            topCandidate != null &&
            hasModifierMismatch(
                catalogTokens = catalogTokens,
                candidateTokens = topCandidateTokens
            )
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType.MODIFIER_MISMATCH
        }

        if (
            tokenRatio != null &&
            tokenRatio < tooGenericTokenRatio
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType.TOO_GENERIC
        }

        if (
            tokenRatio != null &&
            tokenRatio > tooSpecificTokenRatio
        ) {
            signals +=
                RejectedNutritionRetrievalFailureType.TOO_SPECIFIC
        }

        val sortedSignals =
            RejectedNutritionRetrievalFailureType.entries
                .filter {
                    it in signals
                }

        val primaryType =
            PRIMARY_TYPE_PRIORITY
                .firstOrNull {
                    it in signals
                }
                ?: RejectedNutritionRetrievalFailureType.UNKNOWN

        val effectiveSignals =
            sortedSignals.takeIf(List<*>::isNotEmpty)
                ?: listOf(
                    RejectedNutritionRetrievalFailureType.UNKNOWN
                )

        return RejectedNutritionRetrievalFailure(
            catalogKey =
                entry.catalogKey,
            validationStatus =
                entry.validationStatus,
            decisionType =
                entry.decisionType,
            decisionConfidence =
                entry.decisionConfidence,
            selectedServerKey =
                entry.selectedServerKey,
            selectedCandidateRank =
                entry.selectedCandidateRank,
            primaryType =
                primaryType,
            signals =
                effectiveSignals,
            metrics =
                RejectedNutritionRetrievalFailureMetrics(
                    candidateCount =
                        candidates.size,
                    topCandidateScore =
                        topCandidate?.diagnosticScore,
                    secondCandidateScore =
                        secondCandidate?.diagnosticScore,
                    topScoreDelta =
                        topScoreDelta,
                    maximumSharedTokenCount =
                        maximumSharedTokenCount,
                    catalogTokenCount =
                        catalogTokens.size,
                    topCandidateTokenCount =
                        topCandidate
                            ?.let {
                                topCandidateTokens.size
                            },
                    topCandidateTokenRatio =
                        tokenRatio
                ),
            candidates =
                candidates
        )
    }

    private fun createSummary(
        failures: List<RejectedNutritionRetrievalFailure>
    ): RejectedNutritionRetrievalFailureSummary {

        val primaryTypeCounts =
            RejectedNutritionRetrievalFailureType.entries
                .associateWith { type ->
                    failures.count {
                        it.primaryType == type
                    }
                }

        val signalCounts =
            RejectedNutritionRetrievalFailureType.entries
                .associateWith { type ->
                    failures.count {
                        type in it.signals
                    }
                }

        return RejectedNutritionRetrievalFailureSummary(
            rejectedCatalogKeyCount =
                failures.size,
            noMatchCount =
                failures.count {
                    it.validationStatus ==
                            REJECTED_NO_MATCH
                },
            lowConfidenceCount =
                failures.count {
                    it.validationStatus ==
                            REJECTED_LOW_CONFIDENCE
                },
            unknownCount =
                failures.count {
                    it.primaryType ==
                            RejectedNutritionRetrievalFailureType.UNKNOWN
                },
            primaryTypeCounts =
                primaryTypeCounts,
            signalCounts =
                signalCounts
        )
    }

    private fun readEntries(
        file: File
    ): List<SourceEntry> {

        val root =
            JsonParser.parseString(
                file.readText()
            )

        require(root.isJsonObject) {
            "Expected JSON object in ${file.absolutePath}"
        }

        val rootObject =
            root.asJsonObject

        val version =
            rootObject.requiredInt(
                key = "version"
            )

        require(
            version == CANDIDATE_QUALITY_VERSION
        ) {
            "Unsupported rejected candidate quality version: $version"
        }

        val entries =
            rootObject
                .requiredArray(
                    key = "entries"
                )
                .map { element ->

                    val entry =
                        element.asJsonObject

                    SourceEntry(
                        catalogKey =
                            entry
                                .requiredString(
                                    key = "catalogKey"
                                )
                                .let(::normalize),
                        validationStatus =
                            entry
                                .requiredString(
                                    key = "validationStatus"
                                )
                                .uppercase(Locale.ROOT),
                        decisionType =
                            entry.optionalString(
                                key = "decisionType"
                            ),
                        decisionConfidence =
                            entry.optionalDouble(
                                key = "decisionConfidence"
                            ),
                        selectedServerKey =
                            entry.optionalString(
                                key = "selectedServerKey"
                            ),
                        selectedCandidateRank =
                            entry.optionalInt(
                                key = "selectedCandidateRank"
                            ),
                        candidates =
                            entry
                                .requiredArray(
                                    key = "candidates"
                                )
                                .map { candidateElement ->

                                    val candidate =
                                        candidateElement.asJsonObject

                                    SourceCandidate(
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
                                                .mapNotNull {
                                                    it
                                                        .takeIf(
                                                            JsonElement::isJsonPrimitive
                                                        )
                                                        ?.asString
                                                        ?.trim()
                                                        ?.takeIf(
                                                            String::isNotBlank
                                                        )
                                                },
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
            entries
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
            "Duplicate rejected candidate keys: $duplicates"
        }

        return entries
    }

    private fun writeReport(
        report: RejectedNutritionRetrievalFailureReport,
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
        report: RejectedNutritionRetrievalFailureReport,
        outputFile: File,
        output: PrintStream
    ) {
        output.println()
        output.println(SEPARATOR)
        output.println("REJECTED NUTRITION RETRIEVAL FAILURES")
        output.println(SEPARATOR)

        output.println(
            "Rejected catalog keys : " +
                    report.summary.rejectedCatalogKeyCount
        )

        output.println(
            "NO_MATCH             : " +
                    report.summary.noMatchCount
        )

        output.println(
            "LOW_CONFIDENCE       : " +
                    report.summary.lowConfidenceCount
        )

        output.println()
        output.println("Primary classifications:")

        RejectedNutritionRetrievalFailureType.entries
            .forEach { type ->
                output.printf(
                    Locale.ROOT,
                    "  %-28s %6d%n",
                    "${type.name}:",
                    report.summary
                        .primaryTypeCounts
                        .getValue(type)
                )
            }

        output.println()
        output.println("Signals:")

        RejectedNutritionRetrievalFailureType.entries
            .forEach { type ->
                output.printf(
                    Locale.ROOT,
                    "  %-28s %6d%n",
                    "${type.name}:",
                    report.summary
                        .signalCounts
                        .getValue(type)
                )
            }

        output.println()
        output.println(
            "Complete primary classification : " +
                    if (
                        report.summary
                            .primaryTypeCounts
                            .values
                            .sum() ==
                        report.summary.rejectedCatalogKeyCount
                    ) {
                        "YES"
                    } else {
                        "NO"
                    }
        )

        output.println(
            "Report written                  : " +
                    outputFile.path
        )

        output.println(SEPARATOR)
    }

    private fun hasProcessingStateMismatch(
        catalogTokens: Set<String>,
        candidateTokens: Set<String>
    ): Boolean =
        PROCESSING_STATE_GROUPS.any { group ->

            val catalogStates =
                catalogTokens intersect group

            val candidateStates =
                candidateTokens intersect group

            catalogStates.isNotEmpty() &&
                    candidateStates.isNotEmpty() &&
                    catalogStates.intersect(
                        candidateStates
                    ).isEmpty()
        }

    private fun hasProductFormMismatch(
        catalogTokens: Set<String>,
        candidateTokens: Set<String>
    ): Boolean {

        val catalogForms =
            catalogTokens intersect PRODUCT_FORM_TOKENS

        val candidateForms =
            candidateTokens intersect PRODUCT_FORM_TOKENS

        return catalogForms.isNotEmpty() &&
                candidateForms.isNotEmpty() &&
                catalogForms.intersect(
                    candidateForms
                ).isEmpty()
    }

    private fun hasModifierMismatch(
        catalogTokens: Set<String>,
        candidateTokens: Set<String>
    ): Boolean {

        val catalogModifiers =
            catalogTokens intersect MODIFIER_TOKENS

        val candidateModifiers =
            candidateTokens intersect MODIFIER_TOKENS

        return (
                catalogModifiers.isNotEmpty() ||
                        candidateModifiers.isNotEmpty()
                ) &&
                catalogModifiers != candidateModifiers
    }

    private fun tokenize(
        value: String
    ): Set<String> =
        normalize(
            value = value
        )
            .split(' ')
            .filter(String::isNotBlank)
            .toSortedSet()

    private fun normalize(
        value: String
    ): String =
        value
            .trim()
            .lowercase(Locale.ROOT)
            .replace("-", " ")
            .replace("_", " ")
            .replace(
                Regex("[^\\p{L}\\p{N}'& ]+"),
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()

    private fun JsonObject.requiredArray(
        key: String
    ) =
        get(key)
            ?.takeIf(JsonElement::isJsonArray)
            ?.asJsonArray
            ?: error(
                "Missing array '$key'"
            )

    private fun JsonObject.requiredString(
        key: String
    ): String =
        optionalString(key)
            ?: error(
                "Missing string '$key'"
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
        optionalDouble(key)
            ?: error(
                "Missing number '$key'"
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
        optionalInt(key)
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

    private data class SourceEntry(
        val catalogKey: String,
        val validationStatus: String,
        val decisionType: String?,
        val decisionConfidence: Double?,
        val selectedServerKey: String?,
        val selectedCandidateRank: Int?,
        val candidates: List<SourceCandidate>
    )

    private data class SourceCandidate(
        val rank: Int,
        val serverKey: String,
        val diagnosticScore: Double,
        val sharedTokens: List<String>,
        val selected: Boolean
    )

    private companion object {

        val PRIMARY_TYPE_PRIORITY =
            listOf(
                RejectedNutritionRetrievalFailureType.NO_CANDIDATES,
                RejectedNutritionRetrievalFailureType.SELECTED_NOT_TOP_RANKED,
                RejectedNutritionRetrievalFailureType.NO_SHARED_TOKENS,
                RejectedNutritionRetrievalFailureType.PROCESSING_STATE_MISMATCH,
                RejectedNutritionRetrievalFailureType.PRODUCT_FORM_MISMATCH,
                RejectedNutritionRetrievalFailureType.MODIFIER_MISMATCH,
                RejectedNutritionRetrievalFailureType.VERY_LOW_SCORE,
                RejectedNutritionRetrievalFailureType.TOO_GENERIC,
                RejectedNutritionRetrievalFailureType.TOO_SPECIFIC,
                RejectedNutritionRetrievalFailureType.WEAK_TOKEN_OVERLAP,
                RejectedNutritionRetrievalFailureType.SCORE_CLUSTER
            )

        const val CANDIDATE_QUALITY_VERSION =
            1

        const val REJECTED_NO_MATCH =
            "REJECTED_NO_MATCH"

        const val REJECTED_LOW_CONFIDENCE =
            "REJECTED_LOW_CONFIDENCE"

        val PROCESSING_STATE_GROUPS =
            listOf(
                setOf(
                    "fresh",
                    "frozen",
                    "dried",
                    "canned",
                    "cooked",
                    "raw",
                    "smoked",
                    "fried",
                    "roasted",
                    "pickled",
                    "fermented"
                ),
                setOf(
                    "whole",
                    "ground",
                    "minced",
                    "sliced",
                    "chopped",
                    "diced",
                    "grated",
                    "filleted"
                )
            )

        val PRODUCT_FORM_TOKENS =
            setOf(
                "drink",
                "milk",
                "yogurt",
                "cream",
                "cheese",
                "spread",
                "sauce",
                "soup",
                "juice",
                "oil",
                "flour",
                "bread",
                "roll",
                "pasta",
                "rice",
                "sausage",
                "fillet",
                "steak",
                "burger",
                "patty",
                "powder",
                "bar",
                "meal",
                "salad",
                "pudding"
            )

        val MODIFIER_TOKENS =
            setOf(
                "organic",
                "vegan",
                "vegetarian",
                "unsweetened",
                "sweetened",
                "salted",
                "unsalted",
                "lowfat",
                "light",
                "dark",
                "white",
                "red",
                "green",
                "yellow",
                "wholegrain",
                "wholemeal",
                "glutenfree",
                "lactosefree",
                "nonalcoholic",
                "ready"
            )

        const val SEPARATOR =
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    }
}