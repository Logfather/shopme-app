package de.shopme.tools.knowledge.report

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.io.PrintStream
import java.util.Locale
import kotlin.math.floor

data class UnresolvedNutritionRetrievalCandidateSampleReport(
    val version: Int = 1,
    val configuration:
    UnresolvedNutritionRetrievalCandidateSampleConfiguration,
    val summary:
    UnresolvedNutritionRetrievalCandidateSampleSummary,
    val samples:
    List<UnresolvedNutritionRetrievalCandidateSample>
)

data class UnresolvedNutritionRetrievalCandidateSampleConfiguration(
    val highSampleLimit: Int,
    val mediumSampleLimit: Int,
    val lowSampleLimit: Int,
    val missingSampleLimit: Int
)

data class UnresolvedNutritionRetrievalCandidateSampleSummary(
    val unresolvedSourceCount: Int,
    val sampledCount: Int,
    val sourceCountsByScoreBand: Map<String, Int>,
    val sampledCountsByScoreBand: Map<String, Int>
)

data class UnresolvedNutritionRetrievalCandidateSample(
    val catalogKey: String,
    val validationStatus: String,
    val decisionType: String?,
    val decisionConfidence: Double?,
    val scoreBand: String,
    val scoreDeltaBand: String,
    val catalogKeyLengthBand: String,
    val metrics:
    UnresolvedNutritionRetrievalCandidateSampleMetrics,
    val candidates:
    List<UnresolvedNutritionRetrievalCandidateSampleCandidate>
)

data class UnresolvedNutritionRetrievalCandidateSampleMetrics(
    val candidateCount: Int,
    val topCandidateScore: Double?,
    val secondCandidateScore: Double?,
    val topScoreDelta: Double?,
    val maximumSharedTokenCount: Int,
    val catalogTokenCount: Int,
    val topCandidateTokenCount: Int?,
    val topCandidateTokenRatio: Double?
)

data class UnresolvedNutritionRetrievalCandidateSampleCandidate(
    val rank: Int,
    val serverKey: String,
    val diagnosticScore: Double,
    val sharedTokens: List<String>,
    val selected: Boolean
)

data class UnresolvedNutritionRetrievalCandidateSampleResult(
    val report: UnresolvedNutritionRetrievalCandidateSampleReport,
    val outputFile: File
)

/**
 * Erzeugt eine deterministische, nach Score-Bändern geschichtete Stichprobe
 * der noch nicht klassifizierten Nutrition-Retrieval-Fälle.
 *
 * Diese Komponente führt weder Retrieval noch AI-Matching oder Validierung
 * aus und verändert keine bestehenden Artefakte.
 */
class UnresolvedNutritionRetrievalCandidateSampler(
    private val highSampleLimit: Int = 20,
    private val mediumSampleLimit: Int = 20,
    private val lowSampleLimit: Int = 20,
    private val missingSampleLimit: Int = 20
) {

    init {
        require(highSampleLimit >= 0) {
            "highSampleLimit must not be negative."
        }

        require(mediumSampleLimit >= 0) {
            "mediumSampleLimit must not be negative."
        }

        require(lowSampleLimit >= 0) {
            "lowSampleLimit must not be negative."
        }

        require(missingSampleLimit >= 0) {
            "missingSampleLimit must not be negative."
        }
    }

    fun run(
        unresolvedFailureFile: File,
        outputFile: File,
        output: PrintStream = System.out
    ): UnresolvedNutritionRetrievalCandidateSampleResult {

        require(unresolvedFailureFile.isFile) {
            "Unresolved retrieval failure file does not exist: " +
                    unresolvedFailureFile.absolutePath
        }

        val sourceFailures =
            readSourceFailures(
                file = unresolvedFailureFile
            )

        val sourceCountsByScoreBand =
            SCORE_BAND_ORDER
                .associateWith { scoreBand ->
                    sourceFailures.count {
                        it.scoreBand == scoreBand
                    }
                }

        val selected =
            SCORE_BAND_ORDER
                .flatMap { scoreBand ->

                    val group =
                        sourceFailures
                            .filter {
                                it.scoreBand == scoreBand
                            }
                            .sortedWith(
                                compareByDescending<SourceFailure> {
                                    it.metrics.topCandidateScore
                                        ?: Double.NEGATIVE_INFINITY
                                }
                                    .thenBy {
                                        it.catalogKey
                                    }
                            )

                    selectEvenly(
                        values = group,
                        limit =
                            sampleLimit(
                                scoreBand = scoreBand
                            )
                    )
                }
                .sortedWith(
                    compareBy<SourceFailure>(
                        {
                            SCORE_BAND_ORDER.indexOf(
                                it.scoreBand
                            )
                        },
                        {
                            it.catalogKey
                        }
                    )
                )

        val samples =
            selected.map { source ->
                UnresolvedNutritionRetrievalCandidateSample(
                    catalogKey =
                        source.catalogKey,
                    validationStatus =
                        source.validationStatus,
                    decisionType =
                        source.decisionType,
                    decisionConfidence =
                        source.decisionConfidence,
                    scoreBand =
                        source.scoreBand,
                    scoreDeltaBand =
                        source.scoreDeltaBand,
                    catalogKeyLengthBand =
                        source.catalogKeyLengthBand,
                    metrics =
                        source.metrics,
                    candidates =
                        source.candidates
                )
            }

        val sampledCountsByScoreBand =
            SCORE_BAND_ORDER
                .associateWith { scoreBand ->
                    samples.count {
                        it.scoreBand == scoreBand
                    }
                }

        val report =
            UnresolvedNutritionRetrievalCandidateSampleReport(
                configuration =
                    UnresolvedNutritionRetrievalCandidateSampleConfiguration(
                        highSampleLimit =
                            highSampleLimit,
                        mediumSampleLimit =
                            mediumSampleLimit,
                        lowSampleLimit =
                            lowSampleLimit,
                        missingSampleLimit =
                            missingSampleLimit
                    ),
                summary =
                    UnresolvedNutritionRetrievalCandidateSampleSummary(
                        unresolvedSourceCount =
                            sourceFailures.size,
                        sampledCount =
                            samples.size,
                        sourceCountsByScoreBand =
                            sourceCountsByScoreBand,
                        sampledCountsByScoreBand =
                            sampledCountsByScoreBand
                    ),
                samples =
                    samples
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

        return UnresolvedNutritionRetrievalCandidateSampleResult(
            report = report,
            outputFile = outputFile
        )
    }

    private fun sampleLimit(
        scoreBand: String
    ): Int =
        when (scoreBand) {
            SCORE_BAND_HIGH ->
                highSampleLimit

            SCORE_BAND_MEDIUM ->
                mediumSampleLimit

            SCORE_BAND_LOW ->
                lowSampleLimit

            SCORE_BAND_MISSING ->
                missingSampleLimit

            else ->
                0
        }

    /**
     * Wählt über die gesamte sortierte Gruppe verteilte Einträge.
     *
     * Bei 100 Einträgen und Limit 20 wird nicht einfach 0..19 gewählt,
     * sondern gleichmäßig über die gesamte Gruppe verteilt.
     */
    private fun <T> selectEvenly(
        values: List<T>,
        limit: Int
    ): List<T> {

        if (
            limit == 0 ||
            values.isEmpty()
        ) {
            return emptyList()
        }

        if (values.size <= limit) {
            return values
        }

        if (limit == 1) {
            return listOf(
                values[
                    values.size / 2
                ]
            )
        }

        val maximumIndex =
            values.lastIndex.toDouble()

        return (0 until limit)
            .map { position ->

                val fraction =
                    position.toDouble() /
                            (limit - 1).toDouble()

                val index =
                    floor(
                        fraction * maximumIndex
                    )
                        .toInt()

                values[index]
            }
            .distinct()
    }

    private fun readSourceFailures(
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
            "Unsupported unresolved retrieval failure version: $version"
        }

        val failures =
            root.requiredArray(
                key = "failures"
            )
                .map { element ->

                    val failure =
                        element.asJsonObject

                    val metricsObject =
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
                        scoreBand =
                            failure
                                .requiredString(
                                    key = "scoreBand"
                                )
                                .uppercase(Locale.ROOT),
                        scoreDeltaBand =
                            failure
                                .requiredString(
                                    key = "scoreDeltaBand"
                                )
                                .uppercase(Locale.ROOT),
                        catalogKeyLengthBand =
                            failure
                                .requiredString(
                                    key =
                                        "catalogKeyLengthBand"
                                )
                                .uppercase(Locale.ROOT),
                        metrics =
                            UnresolvedNutritionRetrievalCandidateSampleMetrics(
                                candidateCount =
                                    metricsObject.requiredInt(
                                        key = "candidateCount"
                                    ),
                                topCandidateScore =
                                    metricsObject.optionalDouble(
                                        key =
                                            "topCandidateScore"
                                    ),
                                secondCandidateScore =
                                    metricsObject.optionalDouble(
                                        key =
                                            "secondCandidateScore"
                                    ),
                                topScoreDelta =
                                    metricsObject.optionalDouble(
                                        key = "topScoreDelta"
                                    ),
                                maximumSharedTokenCount =
                                    metricsObject.requiredInt(
                                        key =
                                            "maximumSharedTokenCount"
                                    ),
                                catalogTokenCount =
                                    metricsObject.requiredInt(
                                        key = "catalogTokenCount"
                                    ),
                                topCandidateTokenCount =
                                    metricsObject.optionalInt(
                                        key =
                                            "topCandidateTokenCount"
                                    ),
                                topCandidateTokenRatio =
                                    metricsObject.optionalDouble(
                                        key =
                                            "topCandidateTokenRatio"
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

                                    UnresolvedNutritionRetrievalCandidateSampleCandidate(
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
                                                key =
                                                    "diagnosticScore"
                                            ),
                                        sharedTokens =
                                            candidate
                                                .requiredArray(
                                                    key =
                                                        "sharedTokens"
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
                                .sortedBy {
                                    it.rank
                                }
                    )
                }
                .sortedBy {
                    it.catalogKey
                }

        val duplicateKeys =
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

        require(duplicateKeys.isEmpty()) {
            "Duplicate unresolved retrieval candidate keys: " +
                    duplicateKeys
        }

        val unsupportedScoreBands =
            failures
                .map {
                    it.scoreBand
                }
                .filter {
                    it !in SCORE_BAND_ORDER
                }
                .distinct()
                .sorted()

        require(unsupportedScoreBands.isEmpty()) {
            "Unsupported unresolved score bands: " +
                    unsupportedScoreBands
        }

        return failures
    }

    private fun writeReport(
        report: UnresolvedNutritionRetrievalCandidateSampleReport,
        outputFile: File
    ) {
        val parent =
            requireNotNull(
                outputFile.parentFile
            ) {
                "Sample output file has no parent directory: " +
                        outputFile.absolutePath
            }

        if (!parent.exists()) {
            check(parent.mkdirs()) {
                "Could not create sample report directory: " +
                        parent.absolutePath
            }
        }

        require(parent.isDirectory) {
            "Sample output parent is not a directory: " +
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
        report: UnresolvedNutritionRetrievalCandidateSampleReport,
        outputFile: File,
        output: PrintStream
    ) {
        output.println()
        output.println(SEPARATOR)
        output.println("UNRESOLVED NUTRITION RETRIEVAL CANDIDATE SAMPLE")
        output.println(SEPARATOR)

        output.println(
            "Unresolved source keys : " +
                    report.summary.unresolvedSourceCount
        )

        output.println(
            "Sampled keys           : " +
                    report.summary.sampledCount
        )

        output.println()
        output.println("Score-band samples:")

        SCORE_BAND_ORDER.forEach { scoreBand ->

            val sourceCount =
                report.summary
                    .sourceCountsByScoreBand
                    .getValue(scoreBand)

            val sampledCount =
                report.summary
                    .sampledCountsByScoreBand
                    .getValue(scoreBand)

            output.printf(
                Locale.ROOT,
                "  %-8s source=%4d sampled=%3d%n",
                scoreBand,
                sourceCount,
                sampledCount
            )
        }

        output.println()
        output.println(
            "Report written        : " +
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

    private fun JsonObject.requiredObject(
        key: String
    ): JsonObject =
        get(key)
            ?.takeIf(
                JsonElement::isJsonObject
            )
            ?.asJsonObject
            ?: error(
                "Missing object '$key'"
            )

    private fun JsonObject.requiredArray(
        key: String
    ): JsonArray =
        get(key)
            ?.takeIf(
                JsonElement::isJsonArray
            )
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
            ?.takeIf(
                String::isNotBlank
            )

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

    private data class SourceFailure(
        val catalogKey: String,
        val validationStatus: String,
        val decisionType: String?,
        val decisionConfidence: Double?,
        val scoreBand: String,
        val scoreDeltaBand: String,
        val catalogKeyLengthBand: String,
        val metrics:
        UnresolvedNutritionRetrievalCandidateSampleMetrics,
        val candidates:
        List<UnresolvedNutritionRetrievalCandidateSampleCandidate>
    )

    private companion object {

        const val INPUT_VERSION =
            1

        const val SCORE_BAND_HIGH =
            "HIGH"

        const val SCORE_BAND_MEDIUM =
            "MEDIUM"

        const val SCORE_BAND_LOW =
            "LOW"

        const val SCORE_BAND_MISSING =
            "MISSING"

        val SCORE_BAND_ORDER =
            listOf(
                SCORE_BAND_HIGH,
                SCORE_BAND_MEDIUM,
                SCORE_BAND_LOW,
                SCORE_BAND_MISSING
            )

        const val SEPARATOR =
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    }
}