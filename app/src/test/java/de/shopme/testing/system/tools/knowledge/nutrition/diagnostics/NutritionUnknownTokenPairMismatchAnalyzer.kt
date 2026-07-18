package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

class NutritionUnknownTokenPairMismatchAnalyzer(
    private val tokenNormalizer: FoodDomainTokenNormalizer =
        FoodDomainTokenNormalizer(),
    private val foodDomainTokenClassifier:
    DeterministicFoodDomainTokenClassifier =
        DeterministicFoodDomainTokenClassifier(
            normalizer = tokenNormalizer,
        ),
) {

    fun analyze(
        source:
        NutritionUnclassifiedTokenPairMismatchClassification,
        topLimit: Int = DEFAULT_TOP_LIMIT,
    ): NutritionUnknownTokenPairMismatchAnalysis {
        validateSource(
            source = source,
        )

        require(topLimit > 0) {
            "Top limit must be greater than zero: $topLimit"
        }

        val expectedPrimaryUnknownRelationshipCount =
            source.countsByPrimaryRelationshipMismatchType[
                NutritionUnclassifiedTokenPairMismatchType.UNKNOWN
            ] ?: 0

        val unknownSourceEntries =
            source.entries
                .asSequence()
                .filter { entry ->
                    entry.primaryMismatchType ==
                            NutritionUnclassifiedTokenPairMismatchType.UNKNOWN
                }
                .sortedWith(
                    compareBy<NutritionUnclassifiedTokenPairMismatchEntry>(
                        { entry ->
                            entry.catalogKey
                        },
                        { entry ->
                            entry.rank
                        },
                        { entry ->
                            entry.serverKey
                        },
                    ),
                )
                .toList()

        check(
            unknownSourceEntries.size ==
                    expectedPrimaryUnknownRelationshipCount,
        ) {
            "Primary UNKNOWN relationship count does not match source summary: " +
                    "expected=$expectedPrimaryUnknownRelationshipCount, " +
                    "actual=${unknownSourceEntries.size}"
        }

        unknownSourceEntries.forEach { entry ->
            require(entry.observations.isNotEmpty()) {
                "Primary UNKNOWN relationship has no observations: " +
                        "catalogKey=${entry.catalogKey}, " +
                        "serverKey=${entry.serverKey}"
            }

            require(
                entry.observations.all { observation ->
                    observation.mismatchType ==
                            NutritionUnclassifiedTokenPairMismatchType.UNKNOWN
                },
            ) {
                "Primary UNKNOWN relationship contains a non-UNKNOWN " +
                        "observation: catalogKey=${entry.catalogKey}, " +
                        "serverKey=${entry.serverKey}"
            }
        }

        val entries =
            unknownSourceEntries.map { entry ->
                analyzeEntry(
                    entry = entry,
                )
            }

        val observations =
            entries.flatMap { entry ->
                entry.observations
            }

        val countsByCatalogToken =
            observations
                .groupingBy { observation ->
                    observation.normalizedCatalogToken
                }
                .eachCount()
                .toSortedMap()

        val countsByServerToken =
            observations
                .groupingBy { observation ->
                    observation.normalizedServerToken
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

        val countsByCatalogTokenKind =
            enumValues<NutritionUnknownTokenKind>()
                .associateWith { tokenKind ->
                    observations.count { observation ->
                        observation.catalogTokenKind == tokenKind
                    }
                }

        val countsByServerTokenKind =
            enumValues<NutritionUnknownTokenKind>()
                .associateWith { tokenKind ->
                    observations.count { observation ->
                        observation.serverTokenKind == tokenKind
                    }
                }

        val countsByCatalogFoodDomainClass =
            enumValues<FoodDomainTokenClass>()
                .associateWith { tokenClass ->
                    observations.count { observation ->
                        observation.catalogFoodDomainClass == tokenClass
                    }
                }

        val countsByServerFoodDomainClass =
            enumValues<FoodDomainTokenClass>()
                .associateWith { tokenClass ->
                    observations.count { observation ->
                        observation.serverFoodDomainClass == tokenClass
                    }
                }

        val countsByFoodDomainClassPair =
            observations
                .groupingBy { observation ->
                    domainClassPairKey(
                        catalogClass =
                            observation.catalogFoodDomainClass,
                        serverClass =
                            observation.serverFoodDomainClass,
                    )
                }
                .eachCount()
                .toSortedMap()

        val countsByPairProfile =
            enumValues<NutritionUnknownTokenPairProfile>()
                .associateWith { pairProfile ->
                    observations.count { observation ->
                        observation.pairProfile == pairProfile
                    }
                }

        val singleTokenPairRelationshipCount =
            entries.count { entry ->
                entry.singleTokenPair
            }

        val multiTokenRelationshipCount =
            entries.size -
                    singleTokenPairRelationshipCount

        val result =
            NutritionUnknownTokenPairMismatchAnalysis(
                version = 2,
                sourceRelationshipCount =
                    source.sourceRelationshipCount,
                sourceTokenPairObservationCount =
                    source.sourceTokenPairObservationCount,
                sourcePrimaryUnknownRelationshipCount =
                    expectedPrimaryUnknownRelationshipCount,
                analyzedRelationshipCount =
                    entries.size,
                analyzedTokenPairObservationCount =
                    observations.size,
                singleTokenPairRelationshipCount =
                    singleTokenPairRelationshipCount,
                multiTokenRelationshipCount =
                    multiTokenRelationshipCount,
                countsByCatalogToken =
                    countsByCatalogToken,
                countsByServerToken =
                    countsByServerToken,
                countsByTokenPair =
                    countsByTokenPair,
                countsByCatalogTokenKind =
                    countsByCatalogTokenKind,
                countsByServerTokenKind =
                    countsByServerTokenKind,
                countsByCatalogFoodDomainClass =
                    countsByCatalogFoodDomainClass,
                countsByServerFoodDomainClass =
                    countsByServerFoodDomainClass,
                countsByFoodDomainClassPair =
                    countsByFoodDomainClassPair,
                countsByPairProfile =
                    countsByPairProfile,
                countsByCatalogTokenFrequency =
                    createFrequencyDistribution(
                        counts = countsByCatalogToken,
                    ),
                countsByServerTokenFrequency =
                    createFrequencyDistribution(
                        counts = countsByServerToken,
                    ),
                topCatalogTokens =
                    createTopTokenCounts(
                        counts = countsByCatalogToken,
                        topLimit = topLimit,
                    ),
                topServerTokens =
                    createTopTokenCounts(
                        counts = countsByServerToken,
                        topLimit = topLimit,
                    ),
                topTokenPairs =
                    createTopTokenPairCounts(
                        counts = countsByTokenPair,
                        topLimit = topLimit,
                    ),
                topTokenPairsByProfile =
                    enumValues<NutritionUnknownTokenPairProfile>()
                        .associateWith { pairProfile ->
                            createTopTokenPairCounts(
                                counts =
                                    observations
                                        .filter { observation ->
                                            observation.pairProfile ==
                                                    pairProfile
                                        }
                                        .groupingBy { observation ->
                                            observation.pairKey
                                        }
                                        .eachCount(),
                                topLimit =
                                    topLimit,
                            )
                        },
                entries =
                    entries,
            )

        validateResult(
            result = result,
        )

        return result
    }

    private fun analyzeEntry(
        entry:
        NutritionUnclassifiedTokenPairMismatchEntry,
    ): NutritionUnknownTokenPairMismatchEntry {
        val observations =
            entry.observations
                .sortedWith(
                    compareBy<
                            NutritionUnclassifiedTokenPairMismatchObservation
                            >(
                        { observation ->
                            observation.catalogToken
                        },
                        { observation ->
                            observation.serverToken
                        },
                    ),
                )
                .map { observation ->
                    analyzeObservation(
                        observation = observation,
                    )
                }

        return NutritionUnknownTokenPairMismatchEntry(
            catalogKey =
                entry.catalogKey,
            serverKey =
                entry.serverKey,
            rank =
                entry.rank,
            singleTokenPair =
                entry.singleTokenPair,
            observations =
                observations,
        )
    }

    private fun analyzeObservation(
        observation:
        NutritionUnclassifiedTokenPairMismatchObservation,
    ): NutritionUnknownTokenPairObservation {
        val catalogClassification =
            foodDomainTokenClassifier.classify(
                token = observation.catalogToken,
            )

        val serverClassification =
            foodDomainTokenClassifier.classify(
                token = observation.serverToken,
            )

        val catalogTokenKind =
            toLegacyTokenKind(
                tokenClass =
                    catalogClassification.tokenClass,
            )

        val serverTokenKind =
            toLegacyTokenKind(
                tokenClass =
                    serverClassification.tokenClass,
            )

        return NutritionUnknownTokenPairObservation(
            catalogToken =
                observation.catalogToken,
            serverToken =
                observation.serverToken,
            pairKey =
                pairKey(
                    catalogToken =
                        catalogClassification.normalizedToken,
                    serverToken =
                        serverClassification.normalizedToken,
                ),
            normalizedCatalogToken =
                catalogClassification.normalizedToken,
            normalizedServerToken =
                serverClassification.normalizedToken,
            catalogTokenKind =
                catalogTokenKind,
            serverTokenKind =
                serverTokenKind,
            catalogFoodDomainClass =
                catalogClassification.tokenClass,
            serverFoodDomainClass =
                serverClassification.tokenClass,
            pairProfile =
                determinePairProfile(
                    catalogTokenKind = catalogTokenKind,
                    serverTokenKind = serverTokenKind,
                ),
        )
    }

    private fun toLegacyTokenKind(
        tokenClass: FoodDomainTokenClass,
    ): NutritionUnknownTokenKind =
        when (tokenClass) {
            FoodDomainTokenClass.NUMERIC ->
                NutritionUnknownTokenKind.NUMERIC

            FoodDomainTokenClass.STOPWORD ->
                NutritionUnknownTokenKind.STOPWORD

            FoodDomainTokenClass.UNKNOWN ->
                NutritionUnknownTokenKind.UNKNOWN_DOMAIN_TOKEN

            else ->
                NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN
        }

    private fun determinePairProfile(
        catalogTokenKind: NutritionUnknownTokenKind,
        serverTokenKind: NutritionUnknownTokenKind,
    ): NutritionUnknownTokenPairProfile =
        when {
            catalogTokenKind ==
                    NutritionUnknownTokenKind.NUMERIC ||
                    serverTokenKind ==
                    NutritionUnknownTokenKind.NUMERIC ->
                NutritionUnknownTokenPairProfile.NUMERIC_INVOLVED

            catalogTokenKind ==
                    NutritionUnknownTokenKind.STOPWORD &&
                    serverTokenKind ==
                    NutritionUnknownTokenKind.STOPWORD ->
                NutritionUnknownTokenPairProfile.BOTH_STOPWORDS

            catalogTokenKind ==
                    NutritionUnknownTokenKind.STOPWORD ->
                NutritionUnknownTokenPairProfile.CATALOG_STOPWORD

            serverTokenKind ==
                    NutritionUnknownTokenKind.STOPWORD ->
                NutritionUnknownTokenPairProfile.SERVER_STOPWORD

            catalogTokenKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN &&
                    serverTokenKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN ->
                NutritionUnknownTokenPairProfile.BOTH_KNOWN_DOMAIN

            catalogTokenKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN &&
                    serverTokenKind ==
                    NutritionUnknownTokenKind.UNKNOWN_DOMAIN_TOKEN ->
                NutritionUnknownTokenPairProfile
                    .CATALOG_KNOWN_SERVER_UNKNOWN

            catalogTokenKind ==
                    NutritionUnknownTokenKind.UNKNOWN_DOMAIN_TOKEN &&
                    serverTokenKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN ->
                NutritionUnknownTokenPairProfile
                    .CATALOG_UNKNOWN_SERVER_KNOWN

            else ->
                NutritionUnknownTokenPairProfile.BOTH_UNKNOWN_DOMAIN
        }

    private fun createFrequencyDistribution(
        counts: Map<String, Int>,
    ): Map<Int, Int> =
        counts
            .values
            .groupingBy { count ->
                count
            }
            .eachCount()
            .toSortedMap()

    private fun createTopTokenCounts(
        counts: Map<String, Int>,
        topLimit: Int,
    ): List<NutritionUnknownTokenCount> =
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
                NutritionUnknownTokenCount(
                    token =
                        entry.key,
                    count =
                        entry.value,
                )
            }

    private fun createTopTokenPairCounts(
        counts: Map<String, Int>,
        topLimit: Int,
    ): List<NutritionUnknownTokenPairCount> =
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

                NutritionUnknownTokenPairCount(
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

    private fun pairKey(
        catalogToken: String,
        serverToken: String,
    ): String =
        "$catalogToken$PAIR_SEPARATOR$serverToken"

    private fun domainClassPairKey(
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
    ): String =
        "${catalogClass.name}$PAIR_SEPARATOR${serverClass.name}"

    private fun parsePairKey(
        value: String,
    ): Pair<String, String> {
        val separatorIndex =
            value.indexOf(PAIR_SEPARATOR)

        require(separatorIndex >= 0) {
            "Invalid token-pair key: $value"
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
            "Token-pair key has blank catalog token: $value"
        }

        require(serverToken.isNotBlank()) {
            "Token-pair key has blank server token: $value"
        }

        return catalogToken to serverToken
    }

    private fun validateSource(
        source:
        NutritionUnclassifiedTokenPairMismatchClassification,
    ) {
        require(source.version > 0) {
            "Source mismatch classification has an invalid version: " +
                    source.version
        }

        require(
            source.sourceRelationshipCount ==
                    source.classifiedRelationshipCount,
        ) {
            "Source mismatch classification does not cover all relationships."
        }

        require(
            source.sourceTokenPairObservationCount ==
                    source.classifiedTokenPairObservationCount,
        ) {
            "Source mismatch classification does not cover all observations."
        }

        require(
            source.entries.size ==
                    source.classifiedRelationshipCount,
        ) {
            "Source entry count does not match classified relationships."
        }
    }

    private fun validateResult(
        result:
        NutritionUnknownTokenPairMismatchAnalysis,
    ) {
        check(
            result.sourcePrimaryUnknownRelationshipCount ==
                    result.analyzedRelationshipCount,
        ) {
            "Not every primary UNKNOWN relationship was analyzed."
        }

        check(
            result.singleTokenPairRelationshipCount +
                    result.multiTokenRelationshipCount ==
                    result.analyzedRelationshipCount,
        ) {
            "Relationship-shape counts do not cover all relationships."
        }

        check(
            result.countsByTokenPair.values.sum() ==
                    result.analyzedTokenPairObservationCount,
        ) {
            "Token-pair counts do not cover all observations."
        }

        check(
            result.countsByCatalogTokenKind.values.sum() ==
                    result.analyzedTokenPairObservationCount,
        ) {
            "Catalog token-kind counts do not cover all observations."
        }

        check(
            result.countsByServerTokenKind.values.sum() ==
                    result.analyzedTokenPairObservationCount,
        ) {
            "Server token-kind counts do not cover all observations."
        }

        check(
            result.countsByCatalogFoodDomainClass.values.sum() ==
                    result.analyzedTokenPairObservationCount,
        ) {
            "Catalog Food-Domain counts do not cover all observations."
        }

        check(
            result.countsByServerFoodDomainClass.values.sum() ==
                    result.analyzedTokenPairObservationCount,
        ) {
            "Server Food-Domain counts do not cover all observations."
        }

        check(
            result.countsByFoodDomainClassPair.values.sum() ==
                    result.analyzedTokenPairObservationCount,
        ) {
            "Food-Domain class-pair counts do not cover all observations."
        }

        check(
            result.countsByPairProfile.values.sum() ==
                    result.analyzedTokenPairObservationCount,
        ) {
            "Pair-profile counts do not cover all observations."
        }
    }

    private companion object {

        const val DEFAULT_TOP_LIMIT =
            100

        const val PAIR_SEPARATOR =
            " -> "
    }
}