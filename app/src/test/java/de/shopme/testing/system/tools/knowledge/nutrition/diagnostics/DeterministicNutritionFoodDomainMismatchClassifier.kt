package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

class DeterministicNutritionFoodDomainMismatchClassifier {

    fun classify(
        source:
        NutritionUnknownTokenPairMismatchAnalysis,
    ): NutritionFoodDomainMismatchClassification {
        validateSource(
            source = source,
        )

        val entries =
            source.entries
                .sortedWith(
                    compareBy<NutritionUnknownTokenPairMismatchEntry>(
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
                .map { sourceEntry ->
                    classifyEntry(
                        sourceEntry = sourceEntry,
                    )
                }

        val observations =
            entries.flatMap { entry ->
                entry.observations
            }

        val result =
            NutritionFoodDomainMismatchClassification(
                version = 1,
                sourceRelationshipCount =
                    source.analyzedRelationshipCount,
                sourceObservationCount =
                    source.analyzedTokenPairObservationCount,
                classifiedRelationshipCount =
                    entries.size,
                classifiedObservationCount =
                    observations.size,
                countsByPrimaryMismatchType =
                    enumValues<NutritionFoodDomainMismatchType>()
                        .associateWith { mismatchType ->
                            entries.count { entry ->
                                entry.primaryMismatchType ==
                                        mismatchType
                            }
                        },
                countsByObservationMismatchType =
                    enumValues<NutritionFoodDomainMismatchType>()
                        .associateWith { mismatchType ->
                            observations.count { observation ->
                                observation.mismatchType ==
                                        mismatchType
                            }
                        },
                countsByDomainClassPair =
                    observations
                        .groupingBy { observation ->
                            observation.classPairKey
                        }
                        .eachCount()
                        .toSortedMap(),
                entries =
                    entries,
            )

        validateResult(
            result = result,
        )

        return result
    }

    private fun classifyEntry(
        sourceEntry:
        NutritionUnknownTokenPairMismatchEntry,
    ): NutritionFoodDomainMismatchEntry {
        require(sourceEntry.observations.isNotEmpty()) {
            "Food-Domain mismatch source entry has no observations: " +
                    "catalogKey=${sourceEntry.catalogKey}, " +
                    "serverKey=${sourceEntry.serverKey}"
        }

        val observations =
            sourceEntry.observations
                .sortedWith(
                    compareBy<NutritionUnknownTokenPairObservation>(
                        { observation ->
                            observation.normalizedCatalogToken
                        },
                        { observation ->
                            observation.normalizedServerToken
                        },
                    ),
                )
                .map { sourceObservation ->
                    classifyObservation(
                        sourceObservation = sourceObservation,
                    )
                }

        return NutritionFoodDomainMismatchEntry(
            catalogKey =
                sourceEntry.catalogKey,
            serverKey =
                sourceEntry.serverKey,
            rank =
                sourceEntry.rank,
            singleTokenPair =
                sourceEntry.singleTokenPair,
            primaryMismatchType =
                determinePrimaryMismatchType(
                    observations = observations,
                ),
            observations =
                observations,
        )
    }

    private fun classifyObservation(
        sourceObservation:
        NutritionUnknownTokenPairObservation,
    ): NutritionFoodDomainMismatchObservation {
        val catalogClass =
            sourceObservation.catalogFoodDomainClass

        val serverClass =
            sourceObservation.serverFoodDomainClass

        return NutritionFoodDomainMismatchObservation(
            catalogToken =
                sourceObservation.catalogToken,
            serverToken =
                sourceObservation.serverToken,
            normalizedCatalogToken =
                sourceObservation.normalizedCatalogToken,
            normalizedServerToken =
                sourceObservation.normalizedServerToken,
            catalogFoodDomainClass =
                catalogClass,
            serverFoodDomainClass =
                serverClass,
            mismatchType =
                classifyClassPair(
                    catalogClass = catalogClass,
                    serverClass = serverClass,
                ),
            classPairKey =
                classPairKey(
                    catalogClass = catalogClass,
                    serverClass = serverClass,
                ),
        )
    }

    private fun classifyClassPair(
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
    ): NutritionFoodDomainMismatchType {
        if (
            catalogClass in nonSemanticClasses ||
            serverClass in nonSemanticClasses
        ) {
            return NutritionFoodDomainMismatchType
                .NON_SEMANTIC_TOKEN_DIFFERENCE
        }

        if (
            catalogClass == FoodDomainTokenClass.UNKNOWN ||
            serverClass == FoodDomainTokenClass.UNKNOWN
        ) {
            return NutritionFoodDomainMismatchType
                .UNKNOWN_TOKEN_INVOLVED
        }

        if (
            isDietOrSubstituteDifference(
                catalogClass = catalogClass,
                serverClass = serverClass,
            )
        ) {
            return NutritionFoodDomainMismatchType
                .DIET_OR_SUBSTITUTE_DIFFERENCE
        }

        if (
            catalogClass in formAndProcessingClasses &&
            serverClass in formAndProcessingClasses
        ) {
            return NutritionFoodDomainMismatchType
                .FORM_OR_PROCESSING_DIFFERENCE
        }

        if (
            catalogClass in regionAndStyleClasses &&
            serverClass in regionAndStyleClasses
        ) {
            return NutritionFoodDomainMismatchType
                .REGION_OR_STYLE_DIFFERENCE
        }

        if (
            catalogClass == serverClass &&
            catalogClass in entityBearingClasses
        ) {
            return NutritionFoodDomainMismatchType
                .SAME_DOMAIN_DIFFERENT_ENTITY
        }

        if (
            isCompatibleRelationship(
                catalogClass = catalogClass,
                serverClass = serverClass,
            )
        ) {
            return NutritionFoodDomainMismatchType
                .COMPATIBLE_DOMAIN_RELATIONSHIP
        }

        if (
            isCrossDomainMismatch(
                catalogClass = catalogClass,
                serverClass = serverClass,
            )
        ) {
            return NutritionFoodDomainMismatchType
                .CROSS_DOMAIN_MISMATCH
        }

        if (
            catalogClass == serverClass
        ) {
            return when (catalogClass) {
                FoodDomainTokenClass.PRODUCT_FORM,
                FoodDomainTokenClass.PREPARATION_OR_PROCESSING,
                FoodDomainTokenClass.QUANTITY_OR_SIZE_MODIFIER,
                FoodDomainTokenClass.PACKAGING_OR_PRESENTATION,
                    ->
                    NutritionFoodDomainMismatchType
                        .FORM_OR_PROCESSING_DIFFERENCE

                FoodDomainTokenClass.REGION_OR_CUISINE,
                FoodDomainTokenClass.STYLE_OR_QUALITY_MODIFIER,
                FoodDomainTokenClass.COLOR_OR_APPEARANCE,
                    ->
                    NutritionFoodDomainMismatchType
                        .REGION_OR_STYLE_DIFFERENCE

                else ->
                    NutritionFoodDomainMismatchType.UNKNOWN
            }
        }

        return NutritionFoodDomainMismatchType.UNKNOWN
    }

    private fun determinePrimaryMismatchType(
        observations:
        List<NutritionFoodDomainMismatchObservation>,
    ): NutritionFoodDomainMismatchType {
        require(observations.isNotEmpty()) {
            "Cannot determine primary Food-Domain mismatch type " +
                    "without observations."
        }

        return observations
            .map { observation ->
                observation.mismatchType
            }
            .distinct()
            .minBy { mismatchType ->
                primaryPriority.getValue(
                    key = mismatchType,
                )
            }
    }

    private fun isDietOrSubstituteDifference(
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
    ): Boolean {
        if (
            catalogClass !=
            FoodDomainTokenClass.DIET_OR_SUBSTITUTE &&
            serverClass !=
            FoodDomainTokenClass.DIET_OR_SUBSTITUTE
        ) {
            return false
        }

        val otherClass =
            if (
                catalogClass ==
                FoodDomainTokenClass.DIET_OR_SUBSTITUTE
            ) {
                serverClass
            } else {
                catalogClass
            }

        return otherClass in dietConflictClasses
    }

    private fun isCompatibleRelationship(
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
    ): Boolean {
        val unorderedPair =
            setOf(
                catalogClass,
                serverClass,
            )

        return unorderedPair in compatibleClassPairs
    }

    private fun isCrossDomainMismatch(
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
    ): Boolean {
        val unorderedPair =
            setOf(
                catalogClass,
                serverClass,
            )

        if (
            unorderedPair in explicitCrossDomainPairs
        ) {
            return true
        }

        if (
            catalogClass in identityClasses &&
            serverClass in identityClasses &&
            !isCompatibleRelationship(
                catalogClass = catalogClass,
                serverClass = serverClass,
            )
        ) {
            return true
        }

        if (
            catalogClass in identityClasses &&
            serverClass in modifierOnlyClasses
        ) {
            return true
        }

        if (
            serverClass in identityClasses &&
            catalogClass in modifierOnlyClasses
        ) {
            return true
        }

        return false
    }

    private fun classPairKey(
        catalogClass: FoodDomainTokenClass,
        serverClass: FoodDomainTokenClass,
    ): String =
        "${catalogClass.name}$PAIR_SEPARATOR${serverClass.name}"

    private fun validateSource(
        source:
        NutritionUnknownTokenPairMismatchAnalysis,
    ) {
        require(source.version >= 2) {
            "Food-Domain mismatch classification requires " +
                    "Nutrition UNKNOWN analysis version 2 or newer: " +
                    "version=${source.version}"
        }

        require(
            source.analyzedRelationshipCount ==
                    source.entries.size,
        ) {
            "Source entry count does not match analyzed " +
                    "relationship count: analyzed=" +
                    "${source.analyzedRelationshipCount}, " +
                    "entries=${source.entries.size}"
        }

        require(
            source.entries.sumOf { entry ->
                entry.observations.size
            } ==
                    source.analyzedTokenPairObservationCount,
        ) {
            "Source observations do not cover the analyzed " +
                    "observation count."
        }
    }

    private fun validateResult(
        result:
        NutritionFoodDomainMismatchClassification,
    ) {
        check(
            result.sourceRelationshipCount ==
                    result.classifiedRelationshipCount,
        ) {
            "Food-Domain mismatch classification does not cover " +
                    "all source relationships."
        }

        check(
            result.sourceObservationCount ==
                    result.classifiedObservationCount,
        ) {
            "Food-Domain mismatch classification does not cover " +
                    "all source observations."
        }

        check(
            result.countsByPrimaryMismatchType.values.sum() ==
                    result.classifiedRelationshipCount,
        ) {
            "Primary mismatch counts do not cover all relationships."
        }

        check(
            result.countsByObservationMismatchType.values.sum() ==
                    result.classifiedObservationCount,
        ) {
            "Observation mismatch counts do not cover all observations."
        }

        check(
            result.countsByDomainClassPair.values.sum() ==
                    result.classifiedObservationCount,
        ) {
            "Domain-class pair counts do not cover all observations."
        }

        result.entries.forEach { entry ->
            check(entry.observations.isNotEmpty()) {
                "Classified relationship has no observations: " +
                        "catalogKey=${entry.catalogKey}, " +
                        "serverKey=${entry.serverKey}"
            }

            val expectedPrimaryType =
                entry.observations
                    .map { observation ->
                        observation.mismatchType
                    }
                    .distinct()
                    .minBy { mismatchType ->
                        primaryPriority.getValue(
                            key = mismatchType,
                        )
                    }

            check(
                entry.primaryMismatchType ==
                        expectedPrimaryType,
            ) {
                "Relationship primary mismatch type is not " +
                        "deterministic: catalogKey=${entry.catalogKey}, " +
                        "serverKey=${entry.serverKey}, " +
                        "expected=$expectedPrimaryType, " +
                        "actual=${entry.primaryMismatchType}"
            }
        }
    }

    private companion object {

        const val PAIR_SEPARATOR =
            " -> "

        val nonSemanticClasses =
            setOf(
                FoodDomainTokenClass.NUMERIC,
                FoodDomainTokenClass.STOPWORD,
            )

        val formAndProcessingClasses =
            setOf(
                FoodDomainTokenClass.PRODUCT_FORM,
                FoodDomainTokenClass.PREPARATION_OR_PROCESSING,
                FoodDomainTokenClass.QUANTITY_OR_SIZE_MODIFIER,
                FoodDomainTokenClass.PACKAGING_OR_PRESENTATION,
            )

        val regionAndStyleClasses =
            setOf(
                FoodDomainTokenClass.REGION_OR_CUISINE,
                FoodDomainTokenClass.STYLE_OR_QUALITY_MODIFIER,
                FoodDomainTokenClass.COLOR_OR_APPEARANCE,
            )

        val entityBearingClasses =
            setOf(
                FoodDomainTokenClass.ANIMAL_SPECIES,
                FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT,
                FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT,
                FoodDomainTokenClass.PLANT_INGREDIENT,
                FoodDomainTokenClass.GRAIN_OR_LEGUME,
                FoodDomainTokenClass.NUT_SEED_OR_OIL_SOURCE,
                FoodDomainTokenClass.HERB_OR_SPICE,
                FoodDomainTokenClass.DAIRY_PRODUCT,
                FoodDomainTokenClass.DISH_OR_MEAL,
                FoodDomainTokenClass.BAKERY_OR_STARCH_PRODUCT,
                FoodDomainTokenClass.BEVERAGE,
                FoodDomainTokenClass.SWEET_PRODUCT,
                FoodDomainTokenClass.DIET_OR_SUBSTITUTE,
            )

        val identityClasses =
            entityBearingClasses

        val modifierOnlyClasses =
            formAndProcessingClasses +
                    regionAndStyleClasses

        val dietConflictClasses =
            setOf(
                FoodDomainTokenClass.ANIMAL_SPECIES,
                FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT,
                FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT,
            )

        val compatibleClassPairs =
            setOf(
                setOf(
                    FoodDomainTokenClass.HERB_OR_SPICE,
                    FoodDomainTokenClass.PLANT_INGREDIENT,
                ),
                setOf(
                    FoodDomainTokenClass.NUT_SEED_OR_OIL_SOURCE,
                    FoodDomainTokenClass.PLANT_INGREDIENT,
                ),
                setOf(
                    FoodDomainTokenClass.GRAIN_OR_LEGUME,
                    FoodDomainTokenClass.BAKERY_OR_STARCH_PRODUCT,
                ),
                setOf(
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                    FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT,
                ),
                setOf(
                    FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT,
                    FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT,
                ),
                setOf(
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                    FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT,
                ),
                setOf(
                    FoodDomainTokenClass.DAIRY_PRODUCT,
                    FoodDomainTokenClass.DISH_OR_MEAL,
                ),
                setOf(
                    FoodDomainTokenClass.GRAIN_OR_LEGUME,
                    FoodDomainTokenClass.DISH_OR_MEAL,
                ),
                setOf(
                    FoodDomainTokenClass.BAKERY_OR_STARCH_PRODUCT,
                    FoodDomainTokenClass.DISH_OR_MEAL,
                ),
            )

        val explicitCrossDomainPairs =
            setOf(
                setOf(
                    FoodDomainTokenClass.PLANT_INGREDIENT,
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                ),
                setOf(
                    FoodDomainTokenClass.PLANT_INGREDIENT,
                    FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT,
                ),
                setOf(
                    FoodDomainTokenClass.PLANT_INGREDIENT,
                    FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT,
                ),
                setOf(
                    FoodDomainTokenClass.GRAIN_OR_LEGUME,
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                ),
                setOf(
                    FoodDomainTokenClass.NUT_SEED_OR_OIL_SOURCE,
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                ),
                setOf(
                    FoodDomainTokenClass.HERB_OR_SPICE,
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                ),
                setOf(
                    FoodDomainTokenClass.BEVERAGE,
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                ),
                setOf(
                    FoodDomainTokenClass.BEVERAGE,
                    FoodDomainTokenClass.PLANT_INGREDIENT,
                ),
                setOf(
                    FoodDomainTokenClass.SWEET_PRODUCT,
                    FoodDomainTokenClass.ANIMAL_SPECIES,
                ),
            )

        val primaryPriority =
            mapOf(
                NutritionFoodDomainMismatchType
                    .DIET_OR_SUBSTITUTE_DIFFERENCE to 10,

                NutritionFoodDomainMismatchType
                    .CROSS_DOMAIN_MISMATCH to 20,

                NutritionFoodDomainMismatchType
                    .SAME_DOMAIN_DIFFERENT_ENTITY to 30,

                NutritionFoodDomainMismatchType
                    .FORM_OR_PROCESSING_DIFFERENCE to 40,

                NutritionFoodDomainMismatchType
                    .REGION_OR_STYLE_DIFFERENCE to 50,

                NutritionFoodDomainMismatchType
                    .COMPATIBLE_DOMAIN_RELATIONSHIP to 60,

                NutritionFoodDomainMismatchType
                    .UNKNOWN_TOKEN_INVOLVED to 70,

                NutritionFoodDomainMismatchType
                    .NON_SEMANTIC_TOKEN_DIFFERENCE to 80,

                NutritionFoodDomainMismatchType
                    .UNKNOWN to 90,
            )
    }
}