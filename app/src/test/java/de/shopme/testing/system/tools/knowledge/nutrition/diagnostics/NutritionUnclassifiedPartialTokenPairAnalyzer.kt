package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

class NutritionUnclassifiedPartialTokenPairAnalyzer {

    fun analyze(
        source: NutritionPartialCandidateRelationshipAnalysis,
        topLimit: Int = DEFAULT_TOP_LIMIT,
    ): NutritionUnclassifiedPartialTokenPairAnalysis {
        require(source.version > 0) {
            "Source partial relationship analysis has an invalid version: " +
                    source.version
        }

        require(topLimit > 0) {
            "Top limit must be greater than zero: $topLimit"
        }

        require(
            source.partialCandidateCount ==
                    source.classifiedCandidateCount,
        ) {
            "Source partial relationship analysis is incomplete: " +
                    "partial=${source.partialCandidateCount}, " +
                    "classified=${source.classifiedCandidateCount}"
        }

        val expectedUnclassifiedCount =
            source.countsByPrimaryRelationshipType[
                NutritionPartialCandidateRelationshipType
                    .UNCLASSIFIED_PARTIAL
            ] ?: 0

        val unclassifiedRelationships =
            source.entries
                .asSequence()
                .flatMap { entry ->
                    entry.relationships.asSequence()
                }
                .filter { relationship ->
                    relationship.primaryRelationshipType ==
                            NutritionPartialCandidateRelationshipType
                                .UNCLASSIFIED_PARTIAL
                }
                .sortedWith(
                    compareBy<NutritionPartialCandidateRelationship>(
                        { relationship ->
                            relationship.catalogKey
                        },
                        { relationship ->
                            relationship.rank
                        },
                        { relationship ->
                            relationship.serverKey
                        },
                    ),
                )
                .toList()

        check(
            unclassifiedRelationships.size ==
                    expectedUnclassifiedCount,
        ) {
            "Unclassified relationship count does not match source summary: " +
                    "expected=$expectedUnclassifiedCount, " +
                    "actual=${unclassifiedRelationships.size}"
        }

        unclassifiedRelationships.forEach { relationship ->
            require(relationship.catalogOnlyTokens.isNotEmpty()) {
                "Unclassified relationship has no catalog-only tokens: " +
                        "catalogKey=${relationship.catalogKey}, " +
                        "serverKey=${relationship.serverKey}"
            }

            require(relationship.serverOnlyTokens.isNotEmpty()) {
                "Unclassified relationship has no server-only tokens: " +
                        "catalogKey=${relationship.catalogKey}, " +
                        "serverKey=${relationship.serverKey}"
            }
        }

        val entries =
            unclassifiedRelationships.map { relationship ->
                analyzeRelationship(
                    relationship = relationship,
                )
            }

        val observations =
            entries.flatMap { entry ->
                entry.tokenPairs
            }

        val countsByCatalogOnlyToken =
            unclassifiedRelationships
                .flatMap { relationship ->
                    relationship.catalogOnlyTokens
                }
                .groupingBy { token ->
                    token
                }
                .eachCount()
                .toSortedMap()

        val countsByServerOnlyToken =
            unclassifiedRelationships
                .flatMap { relationship ->
                    relationship.serverOnlyTokens
                }
                .groupingBy { token ->
                    token
                }
                .eachCount()
                .toSortedMap()

        val countsByTokenPair =
            observations
                .groupingBy { observation ->
                    observation.pairKey
                }
                .eachCount()
                .toSortedMap()

        val countsByNormalizedTokenPair =
            observations
                .groupingBy { observation ->
                    observation.normalizedPairKey
                }
                .eachCount()
                .toSortedMap()

        val singleTokenPairRelationshipCount =
            entries.count { entry ->
                entry.singleTokenPair
            }

        val multiTokenRelationshipCount =
            entries.size -
                    singleTokenPairRelationshipCount

        val result =
            NutritionUnclassifiedPartialTokenPairAnalysis(
                version = 1,
                sourcePartialCandidateCount =
                    source.partialCandidateCount,
                sourceUnclassifiedPartialCount =
                    expectedUnclassifiedCount,
                analyzedRelationshipCount =
                    entries.size,
                singleTokenPairRelationshipCount =
                    singleTokenPairRelationshipCount,
                multiTokenRelationshipCount =
                    multiTokenRelationshipCount,
                tokenPairObservationCount =
                    observations.size,
                countsByCatalogOnlyToken =
                    countsByCatalogOnlyToken,
                countsByServerOnlyToken =
                    countsByServerOnlyToken,
                countsByTokenPair =
                    countsByTokenPair,
                countsByNormalizedTokenPair =
                    countsByNormalizedTokenPair,
                countsByCatalogOnlyTokenCount =
                    unclassifiedRelationships
                        .groupingBy { relationship ->
                            relationship.catalogOnlyTokens.size
                        }
                        .eachCount()
                        .toSortedMap(),
                countsByServerOnlyTokenCount =
                    unclassifiedRelationships
                        .groupingBy { relationship ->
                            relationship.serverOnlyTokens.size
                        }
                        .eachCount()
                        .toSortedMap(),
                topCatalogOnlyTokens =
                    createTopTokenCounts(
                        counts = countsByCatalogOnlyToken,
                        topLimit = topLimit,
                    ),
                topServerOnlyTokens =
                    createTopTokenCounts(
                        counts = countsByServerOnlyToken,
                        topLimit = topLimit,
                    ),
                topTokenPairs =
                    createTopTokenPairCounts(
                        counts = countsByTokenPair,
                        topLimit = topLimit,
                    ),
                topNormalizedTokenPairs =
                    createTopTokenPairCounts(
                        counts = countsByNormalizedTokenPair,
                        topLimit = topLimit,
                    ),
                entries = entries,
            )

        validateResult(
            result = result,
        )

        return result
    }

    private fun analyzeRelationship(
        relationship:
        NutritionPartialCandidateRelationship,
    ): NutritionUnclassifiedPartialTokenPairEntry {
        val observations =
            relationship.catalogOnlyTokens
                .flatMap { catalogToken ->
                    relationship.serverOnlyTokens.map { serverToken ->
                        createObservation(
                            catalogToken = catalogToken,
                            serverToken = serverToken,
                        )
                    }
                }
                .sortedWith(
                    compareBy<
                            NutritionUnclassifiedPartialTokenPairObservation
                            >(
                        { observation ->
                            observation.catalogToken
                        },
                        { observation ->
                            observation.serverToken
                        },
                    ),
                )

        return NutritionUnclassifiedPartialTokenPairEntry(
            catalogKey =
                relationship.catalogKey,
            serverKey =
                relationship.serverKey,
            rank =
                relationship.rank,
            sharedTokens =
                relationship.sharedTokens.sorted(),
            catalogOnlyTokens =
                relationship.catalogOnlyTokens.sorted(),
            serverOnlyTokens =
                relationship.serverOnlyTokens.sorted(),
            singleTokenPair =
                relationship.catalogOnlyTokens.size == 1 &&
                        relationship.serverOnlyTokens.size == 1,
            tokenPairs =
                observations,
        )
    }

    private fun createObservation(
        catalogToken: String,
        serverToken: String,
    ): NutritionUnclassifiedPartialTokenPairObservation {
        val normalizedCatalogToken =
            normalizeToken(
                token = catalogToken,
            )

        val normalizedServerToken =
            normalizeToken(
                token = serverToken,
            )

        return NutritionUnclassifiedPartialTokenPairObservation(
            catalogToken =
                catalogToken,
            serverToken =
                serverToken,
            pairKey =
                pairKey(
                    catalogToken = catalogToken,
                    serverToken = serverToken,
                ),
            normalizedCatalogToken =
                normalizedCatalogToken,
            normalizedServerToken =
                normalizedServerToken,
            normalizedPairKey =
                pairKey(
                    catalogToken = normalizedCatalogToken,
                    serverToken = normalizedServerToken,
                ),
        )
    }

    private fun createTopTokenCounts(
        counts: Map<String, Int>,
        topLimit: Int,
    ): List<NutritionUnclassifiedPartialTokenCount> =
        counts.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { entry ->
                    entry.value
                }.thenBy { entry ->
                    entry.key
                },
            )
            .take(topLimit)
            .map { entry ->
                NutritionUnclassifiedPartialTokenCount(
                    token = entry.key,
                    count = entry.value,
                )
            }

    private fun createTopTokenPairCounts(
        counts: Map<String, Int>,
        topLimit: Int,
    ): List<NutritionUnclassifiedPartialTokenPairCount> =
        counts.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, Int>> { entry ->
                    entry.value
                }.thenBy { entry ->
                    entry.key
                },
            )
            .take(topLimit)
            .map { entry ->
                val parsedPair =
                    parsePairKey(
                        value = entry.key,
                    )

                NutritionUnclassifiedPartialTokenPairCount(
                    catalogToken =
                        parsedPair.first,
                    serverToken =
                        parsedPair.second,
                    pairKey =
                        entry.key,
                    count =
                        entry.value,
                )
            }

    private fun normalizeToken(
        token: String,
    ): String {
        val normalized =
            token
                .lowercase()
                .trim()

        return singularize(
            token = normalized,
        )
    }

    private fun singularize(
        token: String,
    ): String =
        when {
            token.endsWith("ies") &&
                    token.length > 3 ->
                token.dropLast(3) + "y"

            token.endsWith("ves") &&
                    token.length > 3 ->
                token.dropLast(3) + "f"

            token.endsWith("oes") &&
                    token.length > 3 ->
                token.dropLast(2)

            token.endsWith("ches") &&
                    token.length > 4 ->
                token.dropLast(2)

            token.endsWith("shes") &&
                    token.length > 4 ->
                token.dropLast(2)

            token.endsWith("xes") &&
                    token.length > 3 ->
                token.dropLast(2)

            token.endsWith("zes") &&
                    token.length > 3 ->
                token.dropLast(2)

            token.endsWith("ses") &&
                    token.length > 3 ->
                token.dropLast(2)

            token.endsWith("s") &&
                    token.length > 2 &&
                    !token.endsWith("ss") ->
                token.dropLast(1)

            else ->
                token
        }

    private fun pairKey(
        catalogToken: String,
        serverToken: String,
    ): String =
        "$catalogToken$PAIR_SEPARATOR$serverToken"

    private fun parsePairKey(
        value: String,
    ): Pair<String, String> {
        val separatorIndex =
            value.indexOf(PAIR_SEPARATOR)

        require(separatorIndex >= 0) {
            "Invalid token pair key: $value"
        }

        val catalogToken =
            value.substring(
                startIndex = 0,
                endIndex = separatorIndex,
            )

        val serverToken =
            value.substring(
                startIndex =
                    separatorIndex +
                            PAIR_SEPARATOR.length,
            )

        require(catalogToken.isNotBlank()) {
            "Token pair key has blank catalog token: $value"
        }

        require(serverToken.isNotBlank()) {
            "Token pair key has blank server token: $value"
        }

        return catalogToken to serverToken
    }

    private fun validateResult(
        result:
        NutritionUnclassifiedPartialTokenPairAnalysis,
    ) {
        check(
            result.analyzedRelationshipCount ==
                    result.sourceUnclassifiedPartialCount,
        ) {
            "Not every unclassified relationship was analyzed: " +
                    "source=${result.sourceUnclassifiedPartialCount}, " +
                    "analyzed=${result.analyzedRelationshipCount}"
        }

        check(
            result.singleTokenPairRelationshipCount +
                    result.multiTokenRelationshipCount ==
                    result.analyzedRelationshipCount,
        ) {
            "Single- and multi-token counts do not cover all relationships."
        }

        check(
            result.tokenPairObservationCount ==
                    result.countsByTokenPair.values.sum(),
        ) {
            "Raw token-pair counts do not cover every observation: " +
                    "observations=${result.tokenPairObservationCount}, " +
                    "counted=${result.countsByTokenPair.values.sum()}"
        }

        check(
            result.tokenPairObservationCount ==
                    result.countsByNormalizedTokenPair.values.sum(),
        ) {
            "Normalized token-pair counts do not cover every observation: " +
                    "observations=${result.tokenPairObservationCount}, " +
                    "counted=${result.countsByNormalizedTokenPair.values.sum()}"
        }

        check(
            result.entries.size ==
                    result.analyzedRelationshipCount,
        ) {
            "Entry count does not match analyzed relationship count: " +
                    "entries=${result.entries.size}, " +
                    "analyzed=${result.analyzedRelationshipCount}"
        }
    }

    private companion object {

        const val DEFAULT_TOP_LIMIT =
            100

        const val PAIR_SEPARATOR =
            " -> "
    }
}