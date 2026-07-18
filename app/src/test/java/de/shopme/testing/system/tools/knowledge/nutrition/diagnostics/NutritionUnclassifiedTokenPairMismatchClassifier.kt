package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import java.text.Normalizer
import java.util.Locale

class NutritionUnclassifiedTokenPairMismatchClassifier {

    fun classify(
        source: NutritionUnclassifiedPartialTokenPairAnalysis,
        topLimitPerType: Int = DEFAULT_TOP_LIMIT_PER_TYPE,
    ): NutritionUnclassifiedTokenPairMismatchClassification {
        validateSource(
            source = source,
        )

        require(topLimitPerType > 0) {
            "Top limit per mismatch type must be greater than zero: " +
                    topLimitPerType
        }

        val entries =
            source.entries
                .sortedWith(
                    compareBy<NutritionUnclassifiedPartialTokenPairEntry>(
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
                .map { entry ->
                    classifyEntry(
                        entry = entry,
                    )
                }

        val observations =
            entries.flatMap { entry ->
                entry.observations
            }

        check(
            entries.size ==
                    source.analyzedRelationshipCount,
        ) {
            "Mismatch classification does not cover all source relationships: " +
                    "expected=${source.analyzedRelationshipCount}, " +
                    "actual=${entries.size}"
        }

        check(
            observations.size ==
                    source.tokenPairObservationCount,
        ) {
            "Mismatch classification does not cover all token-pair observations: " +
                    "expected=${source.tokenPairObservationCount}, " +
                    "actual=${observations.size}"
        }

        val countsByMismatchType =
            enumValues<NutritionUnclassifiedTokenPairMismatchType>()
                .associateWith { mismatchType ->
                    observations.count { observation ->
                        observation.mismatchType == mismatchType
                    }
                }

        val countsByPrimaryRelationshipMismatchType =
            enumValues<NutritionUnclassifiedTokenPairMismatchType>()
                .associateWith { mismatchType ->
                    entries.count { entry ->
                        entry.primaryMismatchType == mismatchType
                    }
                }

        val countsByRelationshipMismatchType =
            enumValues<NutritionUnclassifiedTokenPairMismatchType>()
                .associateWith { mismatchType ->
                    entries.count { entry ->
                        mismatchType in entry.detectedMismatchTypes
                    }
                }

        val countsBySingleTokenPair =
            entries
                .groupingBy { entry ->
                    entry.singleTokenPair
                }
                .eachCount()
                .toSortedMap(
                    compareBy<Boolean> { value ->
                        if (value) {
                            0
                        } else {
                            1
                        }
                    },
                )

        val topPairsByMismatchType =
            enumValues<NutritionUnclassifiedTokenPairMismatchType>()
                .associateWith { mismatchType ->
                    createTopPairs(
                        observations =
                            observations.filter { observation ->
                                observation.mismatchType == mismatchType
                            },
                        topLimit =
                            topLimitPerType,
                    )
                }

        val result =
            NutritionUnclassifiedTokenPairMismatchClassification(
                version = 1,
                sourceRelationshipCount =
                    source.analyzedRelationshipCount,
                sourceTokenPairObservationCount =
                    source.tokenPairObservationCount,
                classifiedRelationshipCount =
                    entries.size,
                classifiedTokenPairObservationCount =
                    observations.size,
                countsByMismatchType =
                    countsByMismatchType,
                countsByPrimaryRelationshipMismatchType =
                    countsByPrimaryRelationshipMismatchType,
                countsByRelationshipMismatchType =
                    countsByRelationshipMismatchType,
                countsBySingleTokenPair =
                    countsBySingleTokenPair,
                topPairsByMismatchType =
                    topPairsByMismatchType,
                entries =
                    entries,
            )

        validateResult(
            result = result,
        )

        return result
    }

    private fun classifyEntry(
        entry: NutritionUnclassifiedPartialTokenPairEntry,
    ): NutritionUnclassifiedTokenPairMismatchEntry {
        require(entry.tokenPairs.isNotEmpty()) {
            "Unclassified token-pair entry has no observations: " +
                    "catalogKey=${entry.catalogKey}, " +
                    "serverKey=${entry.serverKey}"
        }

        val observations =
            entry.tokenPairs
                .sortedWith(
                    compareBy<NutritionUnclassifiedPartialTokenPairObservation>(
                        { observation ->
                            observation.catalogToken
                        },
                        { observation ->
                            observation.serverToken
                        },
                    ),
                )
                .map { observation ->
                    classifyObservation(
                        observation = observation,
                    )
                }

        val detectedMismatchTypes =
            observations
                .map { observation ->
                    observation.mismatchType
                }
                .distinct()
                .sortedBy(
                    NutritionUnclassifiedTokenPairMismatchType::ordinal,
                )

        return NutritionUnclassifiedTokenPairMismatchEntry(
            catalogKey =
                entry.catalogKey,
            serverKey =
                entry.serverKey,
            rank =
                entry.rank,
            singleTokenPair =
                entry.singleTokenPair,
            primaryMismatchType =
                determinePrimaryMismatchType(
                    mismatchTypes = detectedMismatchTypes,
                ),
            detectedMismatchTypes =
                detectedMismatchTypes,
            observations =
                observations,
        )
    }

    private fun classifyObservation(
        observation:
        NutritionUnclassifiedPartialTokenPairObservation,
    ): NutritionUnclassifiedTokenPairMismatchObservation {
        val catalogToken =
            normalizeSurfaceToken(
                token = observation.catalogToken,
            )

        val serverToken =
            normalizeSurfaceToken(
                token = observation.serverToken,
            )

        val normalizedCatalogToken =
            normalizeSemanticToken(
                token = catalogToken,
            )

        val normalizedServerToken =
            normalizeSemanticToken(
                token = serverToken,
            )

        val classification =
            classifyTokenPair(
                catalogToken = catalogToken,
                serverToken = serverToken,
                normalizedCatalogToken = normalizedCatalogToken,
                normalizedServerToken = normalizedServerToken,
            )

        return NutritionUnclassifiedTokenPairMismatchObservation(
            catalogToken =
                observation.catalogToken,
            serverToken =
                observation.serverToken,
            pairKey =
                observation.pairKey,
            normalizedCatalogToken =
                normalizedCatalogToken,
            normalizedServerToken =
                normalizedServerToken,
            normalizedPairKey =
                pairKey(
                    catalogToken = normalizedCatalogToken,
                    serverToken = normalizedServerToken,
                ),
            mismatchType =
                classification.type,
            reason =
                classification.reason,
        )
    }

    private fun classifyTokenPair(
        catalogToken: String,
        serverToken: String,
        normalizedCatalogToken: String,
        normalizedServerToken: String,
    ): ClassificationResult {
        if (
            containsDigit(catalogToken) ||
            containsDigit(serverToken)
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .NUMERIC_NOISE,
                reason =
                    "At least one token contains numeric characters.",
            )
        }

        if (
            normalizedCatalogToken ==
            normalizedServerToken
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .SAME_NORMALIZED_TOKEN,
                reason =
                    "Both tokens reduce to the same deterministic normalized form.",
            )
        }

        if (
            isKnownSynonymPair(
                first = normalizedCatalogToken,
                second = normalizedServerToken,
            )
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .POSSIBLE_SYNONYM,
                reason =
                    "The token pair is present in the deterministic synonym dictionary.",
            )
        }

        if (
            normalizedCatalogToken in animalSpeciesTokens &&
            normalizedServerToken in animalSpeciesTokens
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .ANIMAL_SPECIES_MISMATCH,
                reason =
                    "Both tokens identify different animal species or animal-derived primary ingredients.",
            )
        }

        if (
            normalizedCatalogToken in plantOrGrainTokens &&
            normalizedServerToken in plantOrGrainTokens
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .PLANT_OR_GRAIN_MISMATCH,
                reason =
                    "Both tokens identify different plants, legumes, nuts, seeds, or grains.",
            )
        }

        if (
            normalizedCatalogToken in productCategoryTokens ||
            normalizedServerToken in productCategoryTokens
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .PRODUCT_CATEGORY_MISMATCH,
                reason =
                    "At least one token identifies a product or dish category.",
            )
        }

        if (
            normalizedCatalogToken in preparationOrFormTokens ||
            normalizedServerToken in preparationOrFormTokens
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .PREPARATION_OR_FORM_MISMATCH,
                reason =
                    "At least one token identifies a preparation state, cut, texture, or product form.",
            )
        }

        if (
            normalizedCatalogToken in ingredientTokens &&
            normalizedServerToken in ingredientTokens
        ) {
            return ClassificationResult(
                type =
                    NutritionUnclassifiedTokenPairMismatchType
                        .INGREDIENT_MISMATCH,
                reason =
                    "Both tokens identify different primary ingredients.",
            )
        }

        return ClassificationResult(
            type =
                NutritionUnclassifiedTokenPairMismatchType
                    .UNKNOWN,
            reason =
                "No deterministic mismatch rule covers this token pair.",
        )
    }

    private fun determinePrimaryMismatchType(
        mismatchTypes:
        List<NutritionUnclassifiedTokenPairMismatchType>,
    ): NutritionUnclassifiedTokenPairMismatchType {
        require(mismatchTypes.isNotEmpty()) {
            "Cannot determine a primary mismatch type from an empty list."
        }

        return primaryTypePrecedence
            .firstOrNull { mismatchType ->
                mismatchType in mismatchTypes
            }
            ?: NutritionUnclassifiedTokenPairMismatchType.UNKNOWN
    }

    private fun createTopPairs(
        observations:
        List<NutritionUnclassifiedTokenPairMismatchObservation>,
        topLimit: Int,
    ): List<NutritionUnclassifiedTokenPairMismatchCount> =
        observations
            .groupingBy { observation ->
                observation.pairKey
            }
            .eachCount()
            .entries
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

                NutritionUnclassifiedTokenPairMismatchCount(
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

    private fun isKnownSynonymPair(
        first: String,
        second: String,
    ): Boolean {
        val canonicalPair =
            canonicalPairKey(
                first = first,
                second = second,
            )

        return canonicalPair in knownSynonymPairs
    }

    private fun normalizeSurfaceToken(
        token: String,
    ): String =
        Normalizer
            .normalize(
                token,
                Normalizer.Form.NFKD,
            )
            .replace(
                combiningMarkRegex,
                "",
            )
            .lowercase(Locale.ROOT)
            .trim()

    private fun normalizeSemanticToken(
        token: String,
    ): String {
        val normalized =
            normalizeSurfaceToken(
                token = token,
            )

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

    private fun containsDigit(
        token: String,
    ): Boolean =
        token.any(
            Char::isDigit,
        )

    private fun pairKey(
        catalogToken: String,
        serverToken: String,
    ): String =
        "$catalogToken$PAIR_SEPARATOR$serverToken"

    private fun canonicalPairKey(
        first: String,
        second: String,
    ): String =
        listOf(first, second)
            .sorted()
            .joinToString("<->")

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
            "Token-pair key has a blank catalog token: $value"
        }

        require(serverToken.isNotBlank()) {
            "Token-pair key has a blank server token: $value"
        }

        return catalogToken to serverToken
    }

    private fun validateSource(
        source:
        NutritionUnclassifiedPartialTokenPairAnalysis,
    ) {
        require(source.version > 0) {
            "Source token-pair analysis has an invalid version: " +
                    source.version
        }

        require(
            source.sourceUnclassifiedPartialCount ==
                    source.analyzedRelationshipCount,
        ) {
            "Source token-pair relationship analysis is incomplete: " +
                    "source=${source.sourceUnclassifiedPartialCount}, " +
                    "analyzed=${source.analyzedRelationshipCount}"
        }

        require(
            source.entries.size ==
                    source.analyzedRelationshipCount,
        ) {
            "Source entry count does not match analyzed relationships: " +
                    "entries=${source.entries.size}, " +
                    "analyzed=${source.analyzedRelationshipCount}"
        }

        require(
            source.entries.sumOf { entry ->
                entry.tokenPairs.size
            } ==
                    source.tokenPairObservationCount,
        ) {
            "Source entries do not contain the declared number of observations: " +
                    "declared=${source.tokenPairObservationCount}, " +
                    "actual=${
                        source.entries.sumOf { entry ->
                            entry.tokenPairs.size
                        }
                    }"
        }
    }

    private fun validateResult(
        result:
        NutritionUnclassifiedTokenPairMismatchClassification,
    ) {
        check(
            result.sourceRelationshipCount ==
                    result.classifiedRelationshipCount,
        ) {
            "Not every relationship was classified: " +
                    "source=${result.sourceRelationshipCount}, " +
                    "classified=${result.classifiedRelationshipCount}"
        }

        check(
            result.sourceTokenPairObservationCount ==
                    result.classifiedTokenPairObservationCount,
        ) {
            "Not every token-pair observation was classified: " +
                    "source=${result.sourceTokenPairObservationCount}, " +
                    "classified=${result.classifiedTokenPairObservationCount}"
        }

        check(
            result.countsByMismatchType.values.sum() ==
                    result.classifiedTokenPairObservationCount,
        ) {
            "Mismatch-type counts do not cover every token-pair observation."
        }

        check(
            result.countsByPrimaryRelationshipMismatchType.values.sum() ==
                    result.classifiedRelationshipCount,
        ) {
            "Primary relationship mismatch counts do not cover every relationship."
        }

        check(
            result.countsBySingleTokenPair.values.sum() ==
                    result.classifiedRelationshipCount,
        ) {
            "Single-token-pair counts do not cover every relationship."
        }

        check(
            result.entries.size ==
                    result.classifiedRelationshipCount,
        ) {
            "Result entry count does not match classified relationships."
        }
    }

    private data class ClassificationResult(
        val type:
        NutritionUnclassifiedTokenPairMismatchType,
        val reason: String,
    )

    private companion object {

        const val DEFAULT_TOP_LIMIT_PER_TYPE =
            100

        const val PAIR_SEPARATOR =
            " -> "

        const val SYNONYM_PAIR_SEPARATOR =
            " <-> "

        val combiningMarkRegex =
            Regex("\\p{M}+")

        val primaryTypePrecedence =
            listOf(
                NutritionUnclassifiedTokenPairMismatchType
                    .NUMERIC_NOISE,
                NutritionUnclassifiedTokenPairMismatchType
                    .ANIMAL_SPECIES_MISMATCH,
                NutritionUnclassifiedTokenPairMismatchType
                    .PLANT_OR_GRAIN_MISMATCH,
                NutritionUnclassifiedTokenPairMismatchType
                    .INGREDIENT_MISMATCH,
                NutritionUnclassifiedTokenPairMismatchType
                    .PRODUCT_CATEGORY_MISMATCH,
                NutritionUnclassifiedTokenPairMismatchType
                    .PREPARATION_OR_FORM_MISMATCH,
                NutritionUnclassifiedTokenPairMismatchType
                    .SAME_NORMALIZED_TOKEN,
                NutritionUnclassifiedTokenPairMismatchType
                    .POSSIBLE_SYNONYM,
                NutritionUnclassifiedTokenPairMismatchType
                    .UNKNOWN,
            )

        val animalSpeciesTokens =
            setOf(
                "anchovy",
                "andouille",
                "bacon",
                "beef",
                "boar",
                "bratwurst",
                "calf",
                "carp",
                "chicken",
                "cod",
                "crab",
                "duck",
                "eel",
                "fish",
                "goat",
                "goose",
                "ham",
                "herring",
                "lamb",
                "liver",
                "lobster",
                "mackerel",
                "meat",
                "mussel",
                "mutton",
                "octopus",
                "pork",
                "prawn",
                "rabbit",
                "redfish",
                "salami",
                "salmon",
                "sausage",
                "shrimp",
                "squid",
                "steak",
                "tongue",
                "trout",
                "tuna",
                "turkey",
                "veal",
                "venison",
            )

        val plantOrGrainTokens =
            setOf(
                "almond",
                "apple",
                "barley",
                "bean",
                "beet",
                "beetroot",
                "blackbean",
                "buckwheat",
                "cashew",
                "chickpea",
                "coconut",
                "corn",
                "durum",
                "hazelnut",
                "kohlrabi",
                "lentil",
                "lupin",
                "maize",
                "millet",
                "oat",
                "pea",
                "peanut",
                "pistachio",
                "potato",
                "quinoa",
                "rice",
                "rye",
                "seitan",
                "soy",
                "spelt",
                "walnut",
                "wheat",
            )

        val ingredientTokens =
            animalSpeciesTokens +
                    plantOrGrainTokens +
                    setOf(
                        "butter",
                        "cheese",
                        "chocolate",
                        "cream",
                        "egg",
                        "garlic",
                        "honey",
                        "milk",
                        "mushroom",
                        "onion",
                        "quark",
                        "sugar",
                        "tomato",
                        "yogurt",
                    )

        val productCategoryTokens =
            setOf(
                "alternative",
                "bolognese",
                "bread",
                "burger",
                "cake",
                "cappuccino",
                "casserole",
                "cereal",
                "cola",
                "cornflake",
                "crescent",
                "crispbread",
                "croissant",
                "currywurst",
                "dessert",
                "dish",
                "dressing",
                "goulash",
                "lasagna",
                "meal",
                "pasta",
                "pizza",
                "salad",
                "snack",
                "soup",
                "stew",
                "substitute",
                "tagliatelle",
            )

        val preparationOrFormTokens =
            setOf(
                "braid",
                "chopped",
                "cloudy",
                "coarse",
                "cube",
                "deli",
                "dough",
                "fillet",
                "floss",
                "ground",
                "made",
                "plain",
                "precooked",
                "roast",
                "soured",
                "whole",
            )

        val knownSynonymPairs =
            setOf(
                "beet<->beetroot",
                "bellpepper<->capsicum",
                "chickpea<->garbanzo",
                "corn<->maize",
                "courgette<->zucchini",
                "eggplant<->aubergine",
                "scallion<->springonion",
            )
    }
}