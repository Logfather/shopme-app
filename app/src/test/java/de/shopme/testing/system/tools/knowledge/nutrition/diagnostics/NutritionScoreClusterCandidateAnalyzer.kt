package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import java.text.Normalizer
import java.util.Locale

class NutritionScoreClusterCandidateAnalyzer {

    fun analyze(
        report: NutritionScoreClusterDiagnosticReport,
    ): NutritionScoreClusterCandidateAnalysis {

        val entries =
            report.entries
                .sortedBy(
                    NutritionScoreClusterDiagnosticEntry::catalogKey,
                )
                .map { diagnosticEntry ->
                    analyzeEntry(
                        entry = diagnosticEntry,
                    )
                }

        val candidates =
            entries
                .flatMap(
                    NutritionScoreClusterCandidateAnalysisEntry::candidates,
                )

        return NutritionScoreClusterCandidateAnalysis(
            version = 1,
            sourceScoreClusterCount =
                report.scoreClusterCount,
            entryCount =
                entries.size,
            candidateCount =
                candidates.size,
            countsByCandidateCount =
                entries
                    .groupingBy(
                        NutritionScoreClusterCandidateAnalysisEntry::candidateCount,
                    )
                    .eachCount()
                    .toSortedMap(),
            countsBySharedTokenCount =
                candidates
                    .groupingBy(
                        NutritionScoreClusterCandidateAnalysisCandidate::sharedTokenCount,
                    )
                    .eachCount()
                    .toSortedMap(),
            countsByExactTokenSetMatch =
                linkedMapOf(
                    false to
                            candidates.count { candidate ->
                                !candidate.exactTokenSetMatch
                            },
                    true to
                            candidates.count { candidate ->
                                candidate.exactTokenSetMatch
                            },
                ),
            countsByContainmentType =
                enumValues<NutritionScoreClusterContainmentType>()
                    .associateWith { containmentType ->
                        candidates.count { candidate ->
                            candidate.containmentType ==
                                    containmentType
                        }
                    },
            entries = entries,
        )
    }

    private fun analyzeEntry(
        entry: NutritionScoreClusterDiagnosticEntry,
    ): NutritionScoreClusterCandidateAnalysisEntry {

        val catalogTokens =
            tokenize(
                value = entry.catalogKey,
            )

        val candidates =
            entry.candidates
                .sortedWith(
                    compareBy<NutritionScoreClusterCandidate>(
                        NutritionScoreClusterCandidate::rank,
                    ).thenBy(
                        NutritionScoreClusterCandidate::serverKey,
                    ),
                )
                .map { candidate ->
                    analyzeCandidate(
                        catalogTokens = catalogTokens,
                        candidate = candidate,
                    )
                }

        return NutritionScoreClusterCandidateAnalysisEntry(
            catalogKey =
                entry.catalogKey,
            catalogTokens =
                catalogTokens,
            candidateCount =
                candidates.size,
            topCandidateScore =
                entry.topCandidateScore,
            secondCandidateScore =
                entry.secondCandidateScore,
            scoreDelta =
                entry.topScoreDelta,
            candidates =
                candidates,
        )
    }

    private fun analyzeCandidate(
        catalogTokens: List<String>,
        candidate: NutritionScoreClusterCandidate,
    ): NutritionScoreClusterCandidateAnalysisCandidate {

        val serverTokens =
            tokenize(
                value = candidate.serverKey,
            )

        val catalogTokenSet =
            catalogTokens.toSet()

        val serverTokenSet =
            serverTokens.toSet()

        val sharedTokens =
            catalogTokenSet
                .intersect(serverTokenSet)
                .sorted()

        val containmentType =
            classifyContainment(
                catalogTokens = catalogTokenSet,
                serverTokens = serverTokenSet,
                sharedTokens = sharedTokens,
            )

        return NutritionScoreClusterCandidateAnalysisCandidate(
            rank =
                candidate.rank,
            serverKey =
                candidate.serverKey,
            score =
                candidate.score,
            catalogTokens =
                catalogTokens,
            serverTokens =
                serverTokens,
            sharedTokens =
                sharedTokens,
            sharedTokenCount =
                sharedTokens.size,
            catalogTokenCount =
                catalogTokenSet.size,
            serverTokenCount =
                serverTokenSet.size,
            exactTokenSetMatch =
                catalogTokenSet == serverTokenSet,
            containmentType =
                containmentType,
            catalogCoverage =
                calculateCoverage(
                    sharedTokenCount = sharedTokens.size,
                    totalTokenCount = catalogTokenSet.size,
                ),
            serverCoverage =
                calculateCoverage(
                    sharedTokenCount = sharedTokens.size,
                    totalTokenCount = serverTokenSet.size,
                ),
            selected =
                candidate.selected,
        )
    }

    private fun classifyContainment(
        catalogTokens: Set<String>,
        serverTokens: Set<String>,
        sharedTokens: List<String>,
    ): NutritionScoreClusterContainmentType =
        when {
            catalogTokens == serverTokens ->
                NutritionScoreClusterContainmentType.IDENTICAL

            catalogTokens.containsAll(serverTokens) ->
                NutritionScoreClusterContainmentType
                    .CATALOG_CONTAINS_SERVER

            serverTokens.containsAll(catalogTokens) ->
                NutritionScoreClusterContainmentType
                    .SERVER_CONTAINS_CATALOG

            sharedTokens.isNotEmpty() ->
                NutritionScoreClusterContainmentType.PARTIAL

            else ->
                NutritionScoreClusterContainmentType.NONE
        }

    private fun calculateCoverage(
        sharedTokenCount: Int,
        totalTokenCount: Int,
    ): Double =
        if (totalTokenCount == 0) {
            0.0
        } else {
            sharedTokenCount.toDouble() /
                    totalTokenCount.toDouble()
        }

    private fun tokenize(
        value: String,
    ): List<String> =
        normalize(
            value = value,
        )
            .split(TOKEN_SEPARATOR)
            .asSequence()
            .map(String::trim)
            .filter(String::isNotBlank)
            .distinct()
            .sorted()
            .toList()

    private fun normalize(
        value: String,
    ): String =
        Normalizer
            .normalize(
                value.lowercase(Locale.ROOT),
                Normalizer.Form.NFKD,
            )
            .replace(
                COMBINING_MARKS,
                "",
            )
            .replace(
                NON_ALPHANUMERIC,
                " ",
            )
            .trim()

    private companion object {

        val TOKEN_SEPARATOR =
            Regex("\\s+")

        val COMBINING_MARKS =
            Regex("\\p{M}+")

        val NON_ALPHANUMERIC =
            Regex("[^a-z0-9]+")
    }
}