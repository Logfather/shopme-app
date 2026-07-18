package de.shopme.tools.knowledge.report

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.PrintStream
import java.util.Locale

data class RejectedNutritionCandidateQualityReport(
    val version: Int = 1,
    val summary: RejectedNutritionCandidateQualitySummary,
    val entries: List<RejectedNutritionCandidateQualityEntry>
)

data class RejectedNutritionCandidateQualitySummary(
    val rejectedCatalogKeyCount: Int,
    val noCandidateCount: Int,
    val noMatchCount: Int,
    val lowConfidenceCount: Int,
    val candidateCount: Int,
    val averageCandidateCount: Double,
    val averageTopCandidateScore: Double?,
    val averageAllCandidateScore: Double?,
    val selectedLowConfidenceRankCounts: Map<Int, Int>,
    val candidateCountDistribution: Map<Int, Int>
)

data class RejectedNutritionCandidateQualityEntry(
    val catalogKey: String,
    val validationStatus: String,
    val decisionType: String?,
    val decisionConfidence: Double?,
    val decisionReason: String?,
    val validationReason: String?,
    val selectedServerKey: String?,
    val selectedCandidateRank: Int?,
    val candidates: List<RejectedNutritionCandidateQualityCandidate>
)

data class RejectedNutritionCandidateQualityCandidate(
    val rank: Int,
    val serverKey: String,
    val diagnosticScore: Double,
    val sharedTokens: List<String>,
    val selected: Boolean
)

data class RejectedNutritionCandidateQualityReportResult(
    val report: RejectedNutritionCandidateQualityReport,
    val outputFile: File
)

class RejectedNutritionCandidateQualityReporter {

    private val gson =
        GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create()

    fun run(
        requestFile: File,
        diagnosticsFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): RejectedNutritionCandidateQualityReportResult {

        require(requestFile.isFile) {
            "Nutrition match request file does not exist: " +
                    requestFile.absolutePath
        }

        require(diagnosticsFile.isFile) {
            "Nutrition match diagnostics file does not exist: " +
                    diagnosticsFile.absolutePath
        }

        val requests =
            readRequests(
                file = requestFile
            )

        val diagnostics =
            readDiagnostics(
                file = diagnosticsFile
            )

        val entries =
            diagnostics
                .asSequence()
                .filter { diagnostic ->
                    !diagnostic.mappingWritten &&
                            diagnostic.validationStatus in
                            REJECTED_STATUSES
                }
                .map { diagnostic ->

                    val request =
                        requireNotNull(
                            requests[diagnostic.catalogKey]
                        ) {
                            "Missing request for rejected diagnostic: " +
                                    diagnostic.catalogKey
                        }

                    createEntry(
                        request = request,
                        diagnostic = diagnostic
                    )
                }
                .sortedBy {
                    it.catalogKey
                }
                .toList()

        val report =
            RejectedNutritionCandidateQualityReport(
                summary =
                    createSummary(
                        entries = entries
                    ),
                entries =
                    entries
            )

        writeReport(
            report = report,
            file = outputFile
        )

        printReport(
            report = report,
            outputFile = outputFile,
            output = output
        )

        return RejectedNutritionCandidateQualityReportResult(
            report = report,
            outputFile = outputFile
        )
    }

    private fun createEntry(
        request: PersistedRequest,
        diagnostic: PersistedDiagnostic
    ): RejectedNutritionCandidateQualityEntry {

        val normalizedSelectedServerKey =
            diagnostic.selectedServerKey
                ?.let(::normalizeKey)
                ?.takeIf(String::isNotBlank)

        val candidates =
            request.candidates
                .mapIndexed { index, candidate ->

                    val selected =
                        normalizedSelectedServerKey != null &&
                                normalizeKey(candidate.serverKey) ==
                                normalizedSelectedServerKey

                    RejectedNutritionCandidateQualityCandidate(
                        rank = index + 1,
                        serverKey = candidate.serverKey,
                        diagnosticScore =
                            candidate.diagnosticScore,
                        sharedTokens =
                            candidate.sharedTokens,
                        selected = selected
                    )
                }

        val selectedCandidateRank =
            candidates
                .firstOrNull {
                    it.selected
                }
                ?.rank

        return RejectedNutritionCandidateQualityEntry(
            catalogKey =
                diagnostic.catalogKey,
            validationStatus =
                diagnostic.validationStatus,
            decisionType =
                diagnostic.decisionType,
            decisionConfidence =
                diagnostic.confidence,
            decisionReason =
                diagnostic.decisionReason,
            validationReason =
                diagnostic.validationReason,
            selectedServerKey =
                diagnostic.selectedServerKey,
            selectedCandidateRank =
                selectedCandidateRank,
            candidates =
                candidates
        )
    }

    private fun createSummary(
        entries: List<RejectedNutritionCandidateQualityEntry>
    ): RejectedNutritionCandidateQualitySummary {

        val allCandidates =
            entries.flatMap {
                it.candidates
            }

        val topCandidateScores =
            entries.mapNotNull { entry ->
                entry.candidates
                    .firstOrNull()
                    ?.diagnosticScore
            }

        val candidateCountDistribution =
            entries
                .groupingBy {
                    it.candidates.size
                }
                .eachCount()
                .toSortedMap()

        val selectedLowConfidenceRankCounts =
            entries
                .asSequence()
                .filter {
                    it.validationStatus ==
                            STATUS_REJECTED_LOW_CONFIDENCE
                }
                .mapNotNull {
                    it.selectedCandidateRank
                }
                .groupingBy {
                    it
                }
                .eachCount()
                .toSortedMap()

        return RejectedNutritionCandidateQualitySummary(
            rejectedCatalogKeyCount =
                entries.size,
            noCandidateCount =
                entries.count {
                    it.candidates.isEmpty()
                },
            noMatchCount =
                entries.count {
                    it.validationStatus ==
                            STATUS_REJECTED_NO_MATCH
                },
            lowConfidenceCount =
                entries.count {
                    it.validationStatus ==
                            STATUS_REJECTED_LOW_CONFIDENCE
                },
            candidateCount =
                allCandidates.size,
            averageCandidateCount =
                if (entries.isEmpty()) {
                    0.0
                } else {
                    allCandidates.size.toDouble() /
                            entries.size.toDouble()
                },
            averageTopCandidateScore =
                topCandidateScores.averageOrNull(),
            averageAllCandidateScore =
                allCandidates
                    .map {
                        it.diagnosticScore
                    }
                    .averageOrNull(),
            selectedLowConfidenceRankCounts =
                selectedLowConfidenceRankCounts,
            candidateCountDistribution =
                candidateCountDistribution
        )
    }

    private fun readRequests(
        file: File
    ): Map<String, PersistedRequest> {

        val root =
            parseObject(
                file = file
            )

        val requests =
            root.requiredArray(
                key = "requests"
            )
                .map { element ->

                    val requestObject =
                        element.asJsonObject

                    val catalogKey =
                        requestObject
                            .requiredString(
                                key = "catalogKey"
                            )
                            .let(::normalizeKey)

                    val candidates =
                        requestObject
                            .requiredArray(
                                key = "candidates"
                            )
                            .map { candidateElement ->

                                val candidate =
                                    candidateElement.asJsonObject

                                PersistedCandidate(
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
                                            .sorted()
                                )
                            }

                    catalogKey to
                            PersistedRequest(
                                catalogKey = catalogKey,
                                candidates = candidates
                            )
                }

        requireUniqueKeys(
            pairs = requests,
            description =
                "nutrition match requests"
        )

        return requests
            .toMap()
            .toSortedMap()
    }

    private fun readDiagnostics(
        file: File
    ): List<PersistedDiagnostic> {

        val root =
            parseObject(
                file = file
            )

        val version =
            root.requiredInt(
                key = "version"
            )

        require(version == INPUT_VERSION) {
            "Unsupported nutrition match diagnostics version: $version"
        }

        val diagnostics =
            root.requiredArray(
                key = "diagnostics"
            )
                .map { element ->

                    val diagnostic =
                        element.asJsonObject

                    PersistedDiagnostic(
                        catalogKey =
                            diagnostic
                                .requiredString(
                                    key = "catalogKey"
                                )
                                .let(::normalizeKey),
                        decisionType =
                            diagnostic.optionalString(
                                key = "decisionType"
                            ),
                        selectedServerKey =
                            diagnostic.optionalString(
                                key = "selectedServerKey"
                            ),
                        confidence =
                            diagnostic.optionalDouble(
                                key = "confidence"
                            ),
                        decisionReason =
                            diagnostic.optionalString(
                                key = "decisionReason"
                            ),
                        validationStatus =
                            diagnostic
                                .requiredString(
                                    key = "validationStatus"
                                )
                                .uppercase(Locale.ROOT),
                        validationReason =
                            diagnostic.optionalString(
                                key = "validationReason"
                            ),
                        mappingWritten =
                            diagnostic.requiredBoolean(
                                key = "mappingWritten"
                            )
                    )
                }

        requireUniqueKeys(
            pairs =
                diagnostics.map {
                    it.catalogKey to it
                },
            description =
                "nutrition match diagnostics"
        )

        return diagnostics.sortedBy {
            it.catalogKey
        }
    }

    private fun writeReport(
        report: RejectedNutritionCandidateQualityReport,
        file: File
    ) {
        val parent =
            requireNotNull(file.parentFile) {
                "Candidate quality report has no parent directory: " +
                        file.absolutePath
            }

        if (!parent.exists()) {
            check(parent.mkdirs()) {
                "Could not create report directory: " +
                        parent.absolutePath
            }
        }

        require(parent.isDirectory) {
            "Candidate quality report parent is not a directory: " +
                    parent.absolutePath
        }

        file.writeText(
            gson.toJson(report)
        )
    }

    private fun printReport(
        report: RejectedNutritionCandidateQualityReport,
        outputFile: File,
        output: PrintStream
    ) {
        val summary =
            report.summary

        output.println()
        output.println(SEPARATOR)
        output.println("REJECTED NUTRITION CANDIDATE QUALITY")
        output.println(SEPARATOR)
        output.println(
            "Rejected catalog keys      : " +
                    summary.rejectedCatalogKeyCount
        )
        output.println(
            "No candidates              : " +
                    summary.noCandidateCount
        )
        output.println(
            "Rejected NO_MATCH          : " +
                    summary.noMatchCount
        )
        output.println(
            "Rejected low confidence    : " +
                    summary.lowConfidenceCount
        )
        output.println(
            "Candidate rows             : " +
                    summary.candidateCount
        )
        output.println(
            "Average candidates/key     : " +
                    summary.averageCandidateCount.format()
        )
        output.println(
            "Average top score          : " +
                    summary.averageTopCandidateScore.formatNullable()
        )
        output.println(
            "Average candidate score    : " +
                    summary.averageAllCandidateScore.formatNullable()
        )
        output.println(
            "Report written             : " +
                    outputFile.path
        )
        output.println(SEPARATOR)
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
        optionalString(key)
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
        optionalDouble(key)
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
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive &&
                        it.asJsonPrimitive.isNumber
            }
            ?.asInt
            ?: error(
                "Missing integer '$key'"
            )

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

    private fun <T> requireUniqueKeys(
        pairs: List<Pair<String, T>>,
        description: String
    ) {
        val duplicates =
            pairs
                .groupingBy {
                    it.first
                }
                .eachCount()
                .filterValues {
                    it > 1
                }
                .keys
                .sorted()

        require(duplicates.isEmpty()) {
            "Duplicate $description: $duplicates"
        }
    }

    private fun List<Double>.averageOrNull(): Double? =
        if (isEmpty()) {
            null
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

    private fun normalizeKey(
        value: String
    ): String =
        value
            .trim()
            .lowercase(Locale.ROOT)
            .replace(
                Regex("\\s+"),
                " "
            )

    private data class PersistedRequest(
        val catalogKey: String,
        val candidates: List<PersistedCandidate>
    )

    private data class PersistedCandidate(
        val serverKey: String,
        val diagnosticScore: Double,
        val sharedTokens: List<String>
    )

    private data class PersistedDiagnostic(
        val catalogKey: String,
        val decisionType: String?,
        val selectedServerKey: String?,
        val confidence: Double?,
        val decisionReason: String?,
        val validationStatus: String,
        val validationReason: String?,
        val mappingWritten: Boolean
    )

    private companion object {

        const val INPUT_VERSION =
            1

        const val STATUS_REJECTED_NO_MATCH =
            "REJECTED_NO_MATCH"

        const val STATUS_REJECTED_LOW_CONFIDENCE =
            "REJECTED_LOW_CONFIDENCE"

        val REJECTED_STATUSES =
            setOf(
                STATUS_REJECTED_NO_MATCH,
                STATUS_REJECTED_LOW_CONFIDENCE
            )

        const val SEPARATOR =
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    }
}