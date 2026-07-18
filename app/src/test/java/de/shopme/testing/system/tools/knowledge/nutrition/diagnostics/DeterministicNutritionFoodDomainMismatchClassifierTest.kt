package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals

class DeterministicNutritionFoodDomainMismatchClassifierTest {

    private val classifier =
        DeterministicNutritionFoodDomainMismatchClassifier()

    @Test
    fun classifyRepresentativeFoodDomainRelationships() {
        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.PLANT_INGREDIENT,
            serverClass =
                FoodDomainTokenClass.ANIMAL_SPECIES,
            expected =
                NutritionFoodDomainMismatchType
                    .CROSS_DOMAIN_MISMATCH,
        )

        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.ANIMAL_SPECIES,
            serverClass =
                FoodDomainTokenClass.ANIMAL_SPECIES,
            expected =
                NutritionFoodDomainMismatchType
                    .SAME_DOMAIN_DIFFERENT_ENTITY,
        )

        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.PRODUCT_FORM,
            serverClass =
                FoodDomainTokenClass.PREPARATION_OR_PROCESSING,
            expected =
                NutritionFoodDomainMismatchType
                    .FORM_OR_PROCESSING_DIFFERENCE,
        )

        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.REGION_OR_CUISINE,
            serverClass =
                FoodDomainTokenClass.REGION_OR_CUISINE,
            expected =
                NutritionFoodDomainMismatchType
                    .REGION_OR_STYLE_DIFFERENCE,
        )

        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.DIET_OR_SUBSTITUTE,
            serverClass =
                FoodDomainTokenClass.ANIMAL_SPECIES,
            expected =
                NutritionFoodDomainMismatchType
                    .DIET_OR_SUBSTITUTE_DIFFERENCE,
        )

        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.HERB_OR_SPICE,
            serverClass =
                FoodDomainTokenClass.PLANT_INGREDIENT,
            expected =
                NutritionFoodDomainMismatchType
                    .COMPATIBLE_DOMAIN_RELATIONSHIP,
        )

        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.UNKNOWN,
            serverClass =
                FoodDomainTokenClass.ANIMAL_SPECIES,
            expected =
                NutritionFoodDomainMismatchType
                    .UNKNOWN_TOKEN_INVOLVED,
        )

        assertObservationClassification(
            catalogClass =
                FoodDomainTokenClass.STOPWORD,
            serverClass =
                FoodDomainTokenClass.PLANT_INGREDIENT,
            expected =
                NutritionFoodDomainMismatchType
                    .NON_SEMANTIC_TOKEN_DIFFERENCE,
        )
    }

    @Test
    fun strongestMismatchBecomesPrimaryRelationshipType() {
        val source =
            createSource(
                observations =
                    listOf(
                        createObservation(
                            catalogToken = "with",
                            serverToken = "to",
                            catalogClass =
                                FoodDomainTokenClass.STOPWORD,
                            serverClass =
                                FoodDomainTokenClass.STOPWORD,
                        ),
                        createObservation(
                            catalogToken = "leek",
                            serverToken = "beef",
                            catalogClass =
                                FoodDomainTokenClass.PLANT_INGREDIENT,
                            serverClass =
                                FoodDomainTokenClass.ANIMAL_SPECIES,
                        ),
                    ),
            )

        val result =
            classifier.classify(
                source = source,
            )

        assertEquals(
            expected =
                NutritionFoodDomainMismatchType
                    .CROSS_DOMAIN_MISMATCH,
            actual =
                result.entries.single().primaryMismatchType,
        )
    }

    @Test
    fun classificationCoversAllRelationshipsAndObservations() {
        val source =
            createSource(
                observations =
                    listOf(
                        createObservation(
                            catalogToken = "cod",
                            serverToken = "tilapia",
                            catalogClass =
                                FoodDomainTokenClass.ANIMAL_SPECIES,
                            serverClass =
                                FoodDomainTokenClass.ANIMAL_SPECIES,
                        ),
                        createObservation(
                            catalogToken = "kernel",
                            serverToken = "cut",
                            catalogClass =
                                FoodDomainTokenClass.PRODUCT_FORM,
                            serverClass =
                                FoodDomainTokenClass.PRODUCT_FORM,
                        ),
                    ),
            )

        val result =
            classifier.classify(
                source = source,
            )

        assertEquals(
            expected = 1,
            actual = result.classifiedRelationshipCount,
        )

        assertEquals(
            expected = 2,
            actual = result.classifiedObservationCount,
        )

        assertEquals(
            expected = 1,
            actual =
                result.countsByPrimaryMismatchType.values.sum(),
        )

        assertEquals(
            expected = 2,
            actual =
                result.countsByObservationMismatchType.values.sum(),
        )

        assertEquals(
            expected = 2,
            actual =
                result.countsByDomainClassPair.values.sum(),
        )
    }

    private fun assertObservationClassification(
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
        expected: NutritionFoodDomainMismatchType,
    ) {
        val source =
            createSource(
                observations =
                    listOf(
                        createObservation(
                            catalogToken =
                                "catalog-token",
                            serverToken =
                                "server-token",
                            catalogClass =
                                catalogClass,
                            serverClass =
                                serverClass,
                        ),
                    ),
            )

        val result =
            classifier.classify(
                source = source,
            )

        assertEquals(
            expected = expected,
            actual =
                result.entries
                    .single()
                    .observations
                    .single()
                    .mismatchType,
            message =
                "Unexpected Food-Domain mismatch classification " +
                        "for $catalogClass -> $serverClass",
        )
    }

    private fun createSource(
        observations:
        List<NutritionUnknownTokenPairObservation>,
    ): NutritionUnknownTokenPairMismatchAnalysis =
        NutritionUnknownTokenPairMismatchAnalysis(
            version = 2,
            sourceRelationshipCount = 1,
            sourceTokenPairObservationCount =
                observations.size,
            sourcePrimaryUnknownRelationshipCount = 1,
            analyzedRelationshipCount = 1,
            analyzedTokenPairObservationCount =
                observations.size,
            singleTokenPairRelationshipCount =
                if (observations.size == 1) {
                    1
                } else {
                    0
                },
            multiTokenRelationshipCount =
                if (observations.size == 1) {
                    0
                } else {
                    1
                },
            countsByCatalogToken =
                observations
                    .groupingBy { observation ->
                        observation.normalizedCatalogToken
                    }
                    .eachCount(),
            countsByServerToken =
                observations
                    .groupingBy { observation ->
                        observation.normalizedServerToken
                    }
                    .eachCount(),
            countsByTokenPair =
                observations
                    .groupingBy { observation ->
                        observation.pairKey
                    }
                    .eachCount(),
            countsByCatalogTokenKind =
                enumValues<NutritionUnknownTokenKind>()
                    .associateWith { kind ->
                        observations.count { observation ->
                            observation.catalogTokenKind == kind
                        }
                    },
            countsByServerTokenKind =
                enumValues<NutritionUnknownTokenKind>()
                    .associateWith { kind ->
                        observations.count { observation ->
                            observation.serverTokenKind == kind
                        }
                    },
            countsByCatalogFoodDomainClass =
                enumValues<FoodDomainTokenClass>()
                    .associateWith { tokenClass ->
                        observations.count { observation ->
                            observation.catalogFoodDomainClass ==
                                    tokenClass
                        }
                    },
            countsByServerFoodDomainClass =
                enumValues<FoodDomainTokenClass>()
                    .associateWith { tokenClass ->
                        observations.count { observation ->
                            observation.serverFoodDomainClass ==
                                    tokenClass
                        }
                    },
            countsByFoodDomainClassPair =
                observations
                    .groupingBy { observation ->
                        "${observation.catalogFoodDomainClass.name}" +
                                " -> " +
                                observation.serverFoodDomainClass.name
                    }
                    .eachCount(),
            countsByPairProfile =
                enumValues<NutritionUnknownTokenPairProfile>()
                    .associateWith { profile ->
                        observations.count { observation ->
                            observation.pairProfile == profile
                        }
                    },
            countsByCatalogTokenFrequency =
                emptyMap(),
            countsByServerTokenFrequency =
                emptyMap(),
            topCatalogTokens =
                emptyList(),
            topServerTokens =
                emptyList(),
            topTokenPairs =
                emptyList(),
            topTokenPairsByProfile =
                enumValues<NutritionUnknownTokenPairProfile>()
                    .associateWith {
                        emptyList()
                    },
            entries =
                listOf(
                    NutritionUnknownTokenPairMismatchEntry(
                        catalogKey =
                            "catalog product",
                        serverKey =
                            "server product",
                        rank =
                            1,
                        singleTokenPair =
                            observations.size == 1,
                        observations =
                            observations,
                    ),
                ),
        )

    private fun createObservation(
        catalogToken: String,
        serverToken: String,
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
    ): NutritionUnknownTokenPairObservation {
        val catalogKind =
            toLegacyKind(
                tokenClass = catalogClass,
            )

        val serverKind =
            toLegacyKind(
                tokenClass = serverClass,
            )

        return NutritionUnknownTokenPairObservation(
            catalogToken =
                catalogToken,
            serverToken =
                serverToken,
            pairKey =
                "$catalogToken -> $serverToken",
            normalizedCatalogToken =
                catalogToken,
            normalizedServerToken =
                serverToken,
            catalogTokenKind =
                catalogKind,
            serverTokenKind =
                serverKind,
            catalogFoodDomainClass =
                catalogClass,
            serverFoodDomainClass =
                serverClass,
            pairProfile =
                determinePairProfile(
                    catalogKind = catalogKind,
                    serverKind = serverKind,
                ),
        )
    }

    private fun toLegacyKind(
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
        catalogKind: NutritionUnknownTokenKind,
        serverKind: NutritionUnknownTokenKind,
    ): NutritionUnknownTokenPairProfile =
        when {
            catalogKind ==
                    NutritionUnknownTokenKind.NUMERIC ||
                    serverKind ==
                    NutritionUnknownTokenKind.NUMERIC ->
                NutritionUnknownTokenPairProfile.NUMERIC_INVOLVED

            catalogKind ==
                    NutritionUnknownTokenKind.STOPWORD &&
                    serverKind ==
                    NutritionUnknownTokenKind.STOPWORD ->
                NutritionUnknownTokenPairProfile.BOTH_STOPWORDS

            catalogKind ==
                    NutritionUnknownTokenKind.STOPWORD ->
                NutritionUnknownTokenPairProfile.CATALOG_STOPWORD

            serverKind ==
                    NutritionUnknownTokenKind.STOPWORD ->
                NutritionUnknownTokenPairProfile.SERVER_STOPWORD

            catalogKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN &&
                    serverKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN ->
                NutritionUnknownTokenPairProfile.BOTH_KNOWN_DOMAIN

            catalogKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN ->
                NutritionUnknownTokenPairProfile
                    .CATALOG_KNOWN_SERVER_UNKNOWN

            serverKind ==
                    NutritionUnknownTokenKind.KNOWN_DOMAIN_TOKEN ->
                NutritionUnknownTokenPairProfile
                    .CATALOG_UNKNOWN_SERVER_KNOWN

            else ->
                NutritionUnknownTokenPairProfile.BOTH_UNKNOWN_DOMAIN
        }
}