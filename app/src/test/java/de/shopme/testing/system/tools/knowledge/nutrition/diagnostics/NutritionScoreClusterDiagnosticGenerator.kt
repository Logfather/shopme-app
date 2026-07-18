package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

class NutritionScoreClusterDiagnosticGenerator {

    fun generate(
        coverageGapFile: File,
        matchRequestFile: File,
        matchDiagnosticFile: File,
    ): NutritionScoreClusterDiagnosticReport {

        require(coverageGapFile.isFile) {
            "Nutrition coverage-gap report does not exist: " +
                    coverageGapFile.absolutePath
        }

        require(matchRequestFile.isFile) {
            "Nutrition match-request artifact does not exist: " +
                    matchRequestFile.absolutePath
        }

        require(matchDiagnosticFile.isFile) {
            "Nutrition match-diagnostic report does not exist: " +
                    matchDiagnosticFile.absolutePath
        }

        val coverageRoot =
            parseObject(
                file = coverageGapFile,
            )

        val requestRoot =
            parseObject(
                file = matchRequestFile,
            )

        val diagnosticRoot =
            parseObject(
                file = matchDiagnosticFile,
            )

        val scoreClusterGaps =
            findObjectsWithCatalogKey(
                root = coverageRoot,
            )
                .filter { gap ->
                    gap.string("type") == "SCORE_CLUSTER"
                }
                .associateBy { gap ->
                    requireNotNull(
                        gap.string("catalogKey"),
                    ) {
                        "SCORE_CLUSTER gap has no catalogKey."
                    }
                }

        val requestsByCatalogKey =
            findObjectsWithCatalogKey(
                root = requestRoot,
            )
                .filter { candidate ->
                    candidate.hasCandidateCollection()
                }
                .associateByPreferredObject()

        val diagnosticsByCatalogKey =
            findObjectsWithCatalogKey(
                root = diagnosticRoot,
            )
                .filter { candidate ->
                    candidate.hasDiagnosticData()
                }
                .associateByPreferredObject()

        val entries =
            scoreClusterGaps
                .toSortedMap()
                .map { (catalogKey, gap) ->
                    createEntry(
                        catalogKey = catalogKey,
                        gap = gap,
                        request =
                            requestsByCatalogKey[catalogKey],
                        diagnostic =
                            diagnosticsByCatalogKey[catalogKey],
                    )
                }

        return NutritionScoreClusterDiagnosticReport(
            version = 1,
            coverageGapFile =
                coverageGapFile.path,
            matchRequestFile =
                matchRequestFile.path,
            matchDiagnosticFile =
                matchDiagnosticFile.path,
            scoreClusterCount =
                entries.size,
            requestPresentCount =
                entries.count(
                    NutritionScoreClusterDiagnosticEntry::requestPresent,
                ),
            diagnosticPresentCount =
                entries.count(
                    NutritionScoreClusterDiagnosticEntry::diagnosticPresent,
                ),
            averageTopScore =
                entries
                    .mapNotNull(
                        NutritionScoreClusterDiagnosticEntry::topCandidateScore,
                    )
                    .averageOrNull(),
            averageSecondScore =
                entries
                    .mapNotNull(
                        NutritionScoreClusterDiagnosticEntry::secondCandidateScore,
                    )
                    .averageOrNull(),
            averageScoreDelta =
                entries
                    .mapNotNull(
                        NutritionScoreClusterDiagnosticEntry::topScoreDelta,
                    )
                    .averageOrNull(),
            countsByScoreDeltaBucket =
                enumValues<NutritionScoreDeltaBucket>()
                    .associateWith { bucket ->
                        entries.count { entry ->
                            entry.scoreDeltaBucket == bucket
                        }
                    },
            countsBySelectedRank =
                entries
                    .groupingBy { entry ->
                        entry.selectedRank
                            ?.toString()
                            ?: "NONE"
                    }
                    .eachCount()
                    .toSortedMap(),
            countsByDecisionType =
                entries.countNullableStrings(
                    NutritionScoreClusterDiagnosticEntry::decisionType,
                ),
            countsByDecisionSource =
                entries.countNullableStrings(
                    NutritionScoreClusterDiagnosticEntry::decisionSource,
                ),
            countsByValidationStatus =
                entries.countNullableStrings(
                    NutritionScoreClusterDiagnosticEntry::validationStatus,
                ),
            entries = entries,
        )
    }

    private fun createEntry(
        catalogKey: String,
        gap: JsonObject,
        request: JsonObject?,
        diagnostic: JsonObject?,
    ): NutritionScoreClusterDiagnosticEntry {

        val rawCandidates =
            extractCandidateObjects(
                request = request,
                gap = gap,
            )

        val selectedCandidateKey =
            firstString(
                first = diagnostic,
                second = gap,
                names =
                    listOf(
                        "selectedServerKey",
                        "selectedCandidateKey",
                        "selectedKey",
                        "serverKey",
                    ),
            )

        val candidates =
            rawCandidates
                .mapIndexedNotNull { index, candidate ->

                    val serverKey =
                        candidate.firstString(
                            "serverKey",
                            "candidateKey",
                            "key",
                        )
                            ?: return@mapIndexedNotNull null

                    NutritionScoreClusterCandidate(
                        rank = index + 1,
                        serverKey = serverKey,
                        score =
                            candidate.firstDouble(
                                "diagnosticScore",
                                "score",
                                "retrievalScore",
                                "similarityScore",
                            ),
                        sharedTokens =
                            candidate.firstStringList(
                                "sharedTokens",
                                "matchingTokens",
                            ),
                        selected =
                            serverKey == selectedCandidateKey,
                    )
                }

        val topCandidate =
            candidates.getOrNull(0)

        val secondCandidate =
            candidates.getOrNull(1)

        val topCandidateKey =
            gap.firstString(
                "topCandidateKey",
                "topServerKey",
            )
                ?: topCandidate?.serverKey

        val topCandidateScore =
            gap.firstDouble(
                "topCandidateScore",
                "topScore",
            )
                ?: topCandidate?.score

        val secondCandidateKey =
            gap.firstString(
                "secondCandidateKey",
                "secondServerKey",
            )
                ?: secondCandidate?.serverKey

        val secondCandidateScore =
            gap.firstDouble(
                "secondCandidateScore",
                "secondScore",
            )
                ?: secondCandidate?.score

        val topScoreDelta =
            gap.firstDouble(
                "topScoreDelta",
                "scoreDelta",
            )
                ?: calculateScoreDelta(
                    topScore = topCandidateScore,
                    secondScore = secondCandidateScore,
                )

        val selectedRank =
            candidates
                .firstOrNull(
                    NutritionScoreClusterCandidate::selected,
                )
                ?.rank
                ?: firstInt(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "selectedRank",
                            "candidateRank",
                        ),
                )

        val selectedCandidateScore =
            candidates
                .firstOrNull(
                    NutritionScoreClusterCandidate::selected,
                )
                ?.score
                ?: firstDouble(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "selectedCandidateScore",
                            "selectedScore",
                        ),
                )

        return NutritionScoreClusterDiagnosticEntry(
            catalogKey = catalogKey,
            candidateCount =
                if (candidates.isNotEmpty()) {
                    candidates.size
                } else {
                    gap.firstInt(
                        "candidateCount",
                    )
                        ?: 0
                },
            topCandidateKey = topCandidateKey,
            topCandidateScore = topCandidateScore,
            secondCandidateKey = secondCandidateKey,
            secondCandidateScore = secondCandidateScore,
            topScoreDelta = topScoreDelta,
            scoreDeltaBucket =
                NutritionScoreDeltaBucket.classify(
                    scoreDelta = topScoreDelta,
                ),
            topCandidateSharedTokens =
                gap.firstStringList(
                    "topCandidateSharedTokens",
                    "sharedTokens",
                )
                    .ifEmpty {
                        topCandidate
                            ?.sharedTokens
                            .orEmpty()
                    },
            selectedCandidateKey = selectedCandidateKey,
            selectedCandidateScore = selectedCandidateScore,
            selectedRank = selectedRank,
            decisionType =
                firstString(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "decisionType",
                            "decision",
                        ),
                ),
            decisionConfidence =
                firstDouble(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "decisionConfidence",
                            "confidence",
                        ),
                ),
            decisionSource =
                firstString(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "decisionSource",
                            "source",
                        ),
                ),
            validationStatus =
                firstString(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "validationStatus",
                            "status",
                        ),
                ),
            decisionReason =
                firstString(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "decisionReason",
                            "reason",
                        ),
                ),
            validationReason =
                firstString(
                    first = diagnostic,
                    second = gap,
                    names =
                        listOf(
                            "validationReason",
                        ),
                ),
            requestPresent = request != null,
            diagnosticPresent = diagnostic != null,
            candidates = candidates,
        )
    }

    private fun extractCandidateObjects(
        request: JsonObject?,
        gap: JsonObject,
    ): List<JsonObject> {

        val source =
            request
                ?.firstArray(
                    "candidates",
                    "nearestCandidates",
                    "matchCandidates",
                )
                ?: gap.firstArray(
                    "candidates",
                    "nearestCandidates",
                    "matchCandidates",
                )
                ?: return emptyList()

        return source
            .mapNotNull { element ->
                element
                    .takeIf(JsonElement::isJsonObject)
                    ?.asJsonObject
            }
    }

    private fun findObjectsWithCatalogKey(
        root: JsonElement,
    ): List<JsonObject> {

        val result =
            mutableListOf<JsonObject>()

        fun visit(
            element: JsonElement,
        ) {
            when {
                element.isJsonObject -> {
                    val objectValue =
                        element.asJsonObject

                    if (
                        objectValue
                            .string("catalogKey")
                            .isNullOrBlank()
                            .not()
                    ) {
                        result += objectValue
                    }

                    objectValue
                        .entrySet()
                        .forEach { (_, child) ->
                            visit(child)
                        }
                }

                element.isJsonArray ->
                    element
                        .asJsonArray
                        .forEach(::visit)
            }
        }

        visit(root)

        return result
    }

    private fun List<JsonObject>.associateByPreferredObject():
            Map<String, JsonObject> =
        groupBy { objectValue ->
            requireNotNull(
                objectValue.string("catalogKey"),
            )
        }
            .mapValues { (_, values) ->
                values.maxBy { value ->
                    preferredObjectScore(
                        value = value,
                    )
                }
            }

    private fun preferredObjectScore(
        value: JsonObject,
    ): Int =
        buildList {
            if (value.hasCandidateCollection()) {
                add(100)
            }

            if (value.hasDiagnosticData()) {
                add(50)
            }

            add(value.entrySet().size)
        }
            .sum()

    private fun JsonObject.hasCandidateCollection(): Boolean =
        listOf(
            "candidates",
            "nearestCandidates",
            "matchCandidates",
        )
            .any { name ->
                get(name)?.isJsonArray == true
            }

    private fun JsonObject.hasDiagnosticData(): Boolean =
        listOf(
            "decisionType",
            "decisionConfidence",
            "decisionSource",
            "validationStatus",
            "selectedServerKey",
            "selectedCandidateKey",
            "decisionReason",
            "validationReason",
        )
            .any(::has)

    private fun parseObject(
        file: File,
    ): JsonObject =
        file
            .reader()
            .use { reader ->
                JsonParser
                    .parseReader(reader)
                    .asJsonObject
            }

    private fun calculateScoreDelta(
        topScore: Double?,
        secondScore: Double?,
    ): Double? {

        if (
            topScore == null ||
            secondScore == null
        ) {
            return null
        }

        return topScore - secondScore
    }

    private fun firstString(
        first: JsonObject?,
        second: JsonObject?,
        names: List<String>,
    ): String? =
        names
            .asSequence()
            .mapNotNull { name ->
                first?.string(name)
                    ?: second?.string(name)
            }
            .firstOrNull()

    private fun firstDouble(
        first: JsonObject?,
        second: JsonObject?,
        names: List<String>,
    ): Double? =
        names
            .asSequence()
            .mapNotNull { name ->
                first?.double(name)
                    ?: second?.double(name)
            }
            .firstOrNull()

    private fun firstInt(
        first: JsonObject?,
        second: JsonObject?,
        names: List<String>,
    ): Int? =
        names
            .asSequence()
            .mapNotNull { name ->
                first?.int(name)
                    ?: second?.int(name)
            }
            .firstOrNull()

    private fun JsonObject.firstString(
        vararg names: String,
    ): String? =
        names
            .asSequence()
            .mapNotNull { name ->
                string(name)
            }
            .firstOrNull()

    private fun JsonObject.firstDouble(
        vararg names: String,
    ): Double? =
        names
            .asSequence()
            .mapNotNull { name ->
                double(name)
            }
            .firstOrNull()

    private fun JsonObject.firstInt(
        vararg names: String,
    ): Int? =
        names
            .asSequence()
            .mapNotNull { name ->
                int(name)
            }
            .firstOrNull()

    private fun JsonObject.firstArray(
        vararg names: String,
    ): JsonArray? =
        names
            .asSequence()
            .mapNotNull { name ->
                get(name)
                    ?.takeIf(JsonElement::isJsonArray)
                    ?.asJsonArray
            }
            .firstOrNull()

    private fun JsonObject.firstStringList(
        vararg names: String,
    ): List<String> =
        names
            .asSequence()
            .map { name ->
                stringList(name)
            }
            .firstOrNull(List<String>::isNotEmpty)
            .orEmpty()

    private fun JsonObject.string(
        name: String,
    ): String? =
        get(name)
            ?.takeUnless(JsonElement::isJsonNull)
            ?.takeIf(JsonElement::isJsonPrimitive)
            ?.asJsonPrimitive
            ?.takeIf { primitive ->
                primitive.isString
            }
            ?.asString
            ?.trim()
            ?.takeIf(String::isNotBlank)

    private fun JsonObject.double(
        name: String,
    ): Double? =
        get(name)
            ?.takeUnless(JsonElement::isJsonNull)
            ?.takeIf(JsonElement::isJsonPrimitive)
            ?.asJsonPrimitive
            ?.takeIf { primitive ->
                primitive.isNumber
            }
            ?.asDouble

    private fun JsonObject.int(
        name: String,
    ): Int? =
        get(name)
            ?.takeUnless(JsonElement::isJsonNull)
            ?.takeIf(JsonElement::isJsonPrimitive)
            ?.asJsonPrimitive
            ?.takeIf { primitive ->
                primitive.isNumber
            }
            ?.asInt

    private fun JsonObject.stringList(
        name: String,
    ): List<String> =
        get(name)
            ?.takeIf(JsonElement::isJsonArray)
            ?.asJsonArray
            ?.mapNotNull { element ->
                element
                    .takeUnless(JsonElement::isJsonNull)
                    ?.takeIf(JsonElement::isJsonPrimitive)
                    ?.asString
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            }
            ?.distinct()
            ?.sorted()
            .orEmpty()

    private fun List<Double>.averageOrNull(): Double? =
        takeIf(List<Double>::isNotEmpty)
            ?.average()

    private fun List<NutritionScoreClusterDiagnosticEntry>.countNullableStrings(
        selector: (NutritionScoreClusterDiagnosticEntry) -> String?,
    ): Map<String, Int> =
        groupingBy { entry ->
            selector(entry)
                ?: "NONE"
        }
            .eachCount()
            .toSortedMap()
}