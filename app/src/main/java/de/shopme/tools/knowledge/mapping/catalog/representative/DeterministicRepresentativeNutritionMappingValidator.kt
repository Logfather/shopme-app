package de.shopme.tools.knowledge.mapping.catalog.representative

import java.text.Normalizer
import java.util.Locale

class DeterministicRepresentativeNutritionMappingValidator {

    fun validate(
        request: RepresentativeNutritionMappingRequest
    ): RepresentativeNutritionMappingDecision {

        val normalizedRequest =
            request.normalized()

        val catalogKey =
            normalizeKey(
                value = normalizedRequest.catalogKey
            )

        val serverKey =
            normalizeKey(
                value = normalizedRequest.serverKey
            )

        val catalogTokens =
            tokenize(
                value = catalogKey
            )

        val serverTokens =
            tokenize(
                value = serverKey
            )

        if (catalogKey == serverKey) {
            return decision(
                request = normalizedRequest,
                type =
                    RepresentativeNutritionMappingDecisionType.IDENTICAL,
                reasons =
                    setOf(
                        RepresentativeNutritionMappingReason
                            .EXACT_NORMALIZED_KEY
                    )
            )
        }

        val conflictReasons =
            linkedSetOf<RepresentativeNutritionMappingReason>()

        if (
            hasCriticalModifierConflict(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            conflictReasons +=
                RepresentativeNutritionMappingReason
                    .CRITICAL_MODIFIER_CONFLICT
        }

        if (
            hasProcessingStateConflict(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            conflictReasons +=
                RepresentativeNutritionMappingReason
                    .PROCESSING_STATE_CONFLICT
        }

        if (
            hasProductFormConflict(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            conflictReasons +=
                RepresentativeNutritionMappingReason
                    .PRODUCT_FORM_CONFLICT
        }

        if (conflictReasons.isNotEmpty()) {
            return decision(
                request = normalizedRequest,
                type =
                    RepresentativeNutritionMappingDecisionType.INCOMPATIBLE,
                reasons =
                    conflictReasons
            )
        }

        val representativeReasons =
            linkedSetOf<RepresentativeNutritionMappingReason>()

        if (
            hasSameProductClass(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            representativeReasons +=
                RepresentativeNutritionMappingReason
                    .SAME_PRODUCT_CLASS
        }

        if (
            isCompatibleSpecialization(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            representativeReasons +=
                RepresentativeNutritionMappingReason
                    .COMPATIBLE_SPECIALIZATION
        }

        if (
            hasCompatibleVariant(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            representativeReasons +=
                RepresentativeNutritionMappingReason
                    .COMPATIBLE_VARIANT
        }

        if (
            hasCompatiblePreparation(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            representativeReasons +=
                RepresentativeNutritionMappingReason
                    .COMPATIBLE_PREPARATION
        }

        if (
            representativeReasons.isNotEmpty() &&
            hasSufficientEvidence(
                request = normalizedRequest,
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            return decision(
                request = normalizedRequest,
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    representativeReasons
            )
        }

        return decision(
            request = normalizedRequest,
            type =
                RepresentativeNutritionMappingDecisionType.INCOMPATIBLE,
            reasons =
                setOf(
                    RepresentativeNutritionMappingReason
                        .INSUFFICIENT_EVIDENCE
                )
        )
    }

    fun validate(
        requests: Collection<RepresentativeNutritionMappingRequest>
    ): RepresentativeNutritionMappingValidationResult {

        val decisions =
            requests
                .map(::validate)
                .sortedBy {
                    normalizeKey(
                        value = it.catalogKey
                    )
                }

        return RepresentativeNutritionMappingValidationResult(
            decisions = decisions
        )
    }

    private fun decision(
        request: RepresentativeNutritionMappingRequest,
        type: RepresentativeNutritionMappingDecisionType,
        reasons: Collection<RepresentativeNutritionMappingReason>
    ): RepresentativeNutritionMappingDecision =
        RepresentativeNutritionMappingDecision(
            catalogKey =
                request.catalogKey,
            serverKey =
                request.serverKey,
            type =
                type,
            reasons =
                reasons
                    .distinct()
                    .sortedBy {
                        it.ordinal
                    }
        )

    private fun hasSufficientEvidence(
        request: RepresentativeNutritionMappingRequest,
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean {

        val sharedCoreTokens =
            coreTokens(catalogTokens)
                .intersect(
                    coreTokens(serverTokens)
                )

        if (sharedCoreTokens.isNotEmpty()) {
            return true
        }

        if (
            request.sharedTokens
                .map(::canonicalToken)
                .any {
                    it.isNotBlank()
                }
        ) {
            return true
        }

        if (
            isGenericPreparedMeal(
                tokens = catalogTokens
            ) &&
            isSpecificPreparedMeal(
                tokens = serverTokens
            )
        ) {
            return true
        }

        return false
    }

    private fun hasSameProductClass(
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean {

        val catalogClasses =
            productClasses(
                tokens = catalogTokens
            )

        val serverClasses =
            productClasses(
                tokens = serverTokens
            )

        if (
            catalogClasses.isNotEmpty() &&
            serverClasses.isNotEmpty() &&
            catalogClasses.intersect(
                serverClasses
            ).isNotEmpty()
        ) {
            return true
        }

        if (
            isGenericPreparedMeal(
                tokens = catalogTokens
            ) &&
            isSpecificPreparedMeal(
                tokens = serverTokens
            )
        ) {
            return true
        }

        return coreTokens(catalogTokens)
            .intersect(
                coreTokens(serverTokens)
            )
            .isNotEmpty()
    }

    private fun isCompatibleSpecialization(
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean {

        val catalogCore =
            coreTokens(
                tokens = catalogTokens
            )

        val serverCore =
            coreTokens(
                tokens = serverTokens
            )

        if (
            catalogCore.isNotEmpty() &&
            serverCore.containsAll(
                catalogCore
            )
        ) {
            return true
        }

        if (
            serverCore.isNotEmpty() &&
            catalogCore.containsAll(
                serverCore
            )
        ) {
            return true
        }

        return isGenericPreparedMeal(
            tokens = catalogTokens
        ) &&
                isSpecificPreparedMeal(
                    tokens = serverTokens
                )
    }

    private fun hasCompatibleVariant(
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean {

        val catalogClasses =
            productClasses(
                tokens = catalogTokens
            )

        val serverClasses =
            productClasses(
                tokens = serverTokens
            )

        if (
            catalogClasses.intersect(
                serverClasses
            ).isEmpty()
        ) {
            return false
        }

        val catalogCore =
            coreTokens(
                tokens = catalogTokens
            )

        val serverCore =
            coreTokens(
                tokens = serverTokens
            )

        return catalogCore != serverCore
    }

    private fun hasCompatiblePreparation(
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean {

        if (
            hasProcessingStateConflict(
                catalogTokens = catalogTokens,
                serverTokens = serverTokens
            )
        ) {
            return false
        }

        val catalogPreparation =
            catalogTokens intersect
                    NON_CRITICAL_PREPARATION_TOKENS

        val serverPreparation =
            serverTokens intersect
                    NON_CRITICAL_PREPARATION_TOKENS

        return catalogPreparation != serverPreparation &&
                (
                        catalogPreparation.isNotEmpty() ||
                                serverPreparation.isNotEmpty()
                        )
    }

    private fun hasCriticalModifierConflict(
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean =
        CRITICAL_MODIFIER_GROUPS.any { group ->

            val catalogModifiers =
                catalogTokens intersect group

            val serverModifiers =
                serverTokens intersect group

            catalogModifiers.isNotEmpty() &&
                    serverModifiers.isNotEmpty() &&
                    catalogModifiers.intersect(
                        serverModifiers
                    ).isEmpty()
        }

    private fun hasProcessingStateConflict(
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean =
        PROCESSING_STATE_GROUPS.any { group ->

            val catalogStates =
                catalogTokens intersect group

            val serverStates =
                serverTokens intersect group

            catalogStates.isNotEmpty() &&
                    serverStates.isNotEmpty() &&
                    catalogStates.intersect(
                        serverStates
                    ).isEmpty()
        }

    private fun hasProductFormConflict(
        catalogTokens: Set<String>,
        serverTokens: Set<String>
    ): Boolean {

        val catalogClasses =
            productClasses(
                tokens = catalogTokens
            )

        val serverClasses =
            productClasses(
                tokens = serverTokens
            )

        if (
            catalogClasses.isEmpty() ||
            serverClasses.isEmpty()
        ) {
            return false
        }

        if (
            catalogClasses.intersect(
                serverClasses
            ).isNotEmpty()
        ) {
            return false
        }

        if (
            isGenericPreparedMeal(
                tokens = catalogTokens
            ) &&
            isSpecificPreparedMeal(
                tokens = serverTokens
            )
        ) {
            return false
        }

        return true
    }

    private fun productClasses(
        tokens: Set<String>
    ): Set<String> =
        PRODUCT_CLASS_TOKENS
            .mapNotNull { (productClass, classTokens) ->
                productClass.takeIf {
                    tokens.intersect(
                        classTokens
                    ).isNotEmpty()
                }
            }
            .toSet()

    private fun coreTokens(
        tokens: Set<String>
    ): Set<String> =
        tokens
            .asSequence()
            .filterNot {
                it in NON_CORE_TOKENS
            }
            .filterNot {
                PRODUCT_CLASS_TOKENS
                    .values
                    .any { classTokens ->
                        it in classTokens
                    }
            }
            .toSet()

    private fun isGenericPreparedMeal(
        tokens: Set<String>
    ): Boolean =
        (
                "meal" in tokens ||
                        "dish" in tokens ||
                        "readymeal" in tokens
                ) &&
                (
                        "ready" in tokens ||
                                "prepared" in tokens ||
                                "meal" in tokens ||
                                "dish" in tokens
                        )

    private fun isSpecificPreparedMeal(
        tokens: Set<String>
    ): Boolean =
        tokens.intersect(
            SPECIFIC_PREPARED_MEAL_TOKENS
        )
            .isNotEmpty()

    private fun tokenize(
        value: String
    ): Set<String> =
        normalizeKey(
            value = value
        )
            .split(' ')
            .asSequence()
            .map(::canonicalToken)
            .filter(String::isNotBlank)
            .toSortedSet()

    private fun canonicalToken(
        value: String
    ): String {

        val token =
            normalizeKey(
                value = value
            )
                .replace(
                    " ",
                    ""
                )

        return when {
            token == "yoghurt" ->
                "yogurt"

            token == "beverages" ||
                    token == "beverage" ->
                "drink"

            token == "wraps" ->
                "wrap"

            token == "sausages" ->
                "sausage"

            token == "meals" ->
                "meal"

            token == "dishes" ->
                "dish"

            token == "vegetables" ->
                "vegetable"

            token == "fruits" ->
                "fruit"

            token == "tomatoes" ->
                "tomato"

            token == "potatoes" ->
                "potato"

            token == "cherries" ->
                "cherry"

            token.endsWith("s") &&
                    token.length > 4 ->
                token.dropLast(1)

            else ->
                token
        }
    }

    private fun normalizeKey(
        value: String
    ): String {

        val decomposed =
            Normalizer.normalize(
                value,
                Normalizer.Form.NFKD
            )

        return decomposed
            .replace(
                Regex("\\p{M}+"),
                ""
            )
            .lowercase(
                Locale.ROOT
            )
            .replace(
                "-",
                " "
            )
            .replace(
                "_",
                " "
            )
            .replace(
                Regex("[^\\p{L}\\p{N} ]+"),
                " "
            )
            .replace(
                Regex("\\s+"),
                " "
            )
            .trim()
    }

    private companion object {

        val PRODUCT_CLASS_TOKENS =
            linkedMapOf(
                "drink" to
                        setOf(
                            "drink",
                            "juice",
                            "smoothie",
                            "water",
                            "milk",
                            "coffee",
                            "tea",
                            "latte"
                        ),
                "yogurt" to
                        setOf(
                            "yogurt",
                            "kefir"
                        ),
                "cheese" to
                        setOf(
                            "cheese",
                            "quark"
                        ),
                "spread" to
                        setOf(
                            "spread",
                            "paste",
                            "pate"
                        ),
                "soup" to
                        setOf(
                            "soup",
                            "broth"
                        ),
                "sauce" to
                        setOf(
                            "sauce",
                            "dressing",
                            "ketchup",
                            "mustard"
                        ),
                "bread" to
                        setOf(
                            "bread",
                            "roll",
                            "baguette",
                            "toast"
                        ),
                "pasta" to
                        setOf(
                            "pasta",
                            "spaghetti",
                            "macaroni",
                            "tagliatelle",
                            "noodle",
                            "tortellini"
                        ),
                "rice" to
                        setOf(
                            "rice",
                            "risotto"
                        ),
                "sausage" to
                        setOf(
                            "sausage",
                            "salami",
                            "bratwurst",
                            "mettwurst",
                            "teewurst"
                        ),
                "wrap" to
                        setOf(
                            "wrap",
                            "burrito"
                        ),
                "meal" to
                        setOf(
                            "meal",
                            "dish",
                            "bolognese",
                            "lasagna",
                            "curry",
                            "goulash",
                            "stew",
                            "casserole",
                            "gratin",
                            "paella",
                            "fricassee"
                        ),
                "oil" to
                        setOf(
                            "oil",
                            "fat"
                        ),
                "flour" to
                        setOf(
                            "flour",
                            "meal",
                            "semolina"
                        ),
                "meat" to
                        setOf(
                            "meat",
                            "beef",
                            "pork",
                            "veal",
                            "lamb",
                            "venison",
                            "chicken",
                            "turkey",
                            "goose",
                            "duck"
                        ),
                "fish" to
                        setOf(
                            "fish",
                            "salmon",
                            "trout",
                            "cod",
                            "tuna",
                            "plaice",
                            "herring"
                        ),
                "dessert" to
                        setOf(
                            "dessert",
                            "pudding",
                            "cake",
                            "cookie"
                        )
            )

        val CRITICAL_MODIFIER_GROUPS =
            listOf(
                setOf(
                    "vegan",
                    "meat",
                    "beef",
                    "pork",
                    "chicken",
                    "turkey",
                    "fish"
                ),
                setOf(
                    "vegetarian",
                    "meat",
                    "beef",
                    "pork",
                    "chicken",
                    "turkey",
                    "fish"
                ),
                setOf(
                    "sweetened",
                    "unsweetened",
                    "sugarfree"
                ),
                setOf(
                    "alcoholic",
                    "nonalcoholic"
                ),
                setOf(
                    "glutenfree",
                    "wheat",
                    "rye",
                    "barley"
                ),
                setOf(
                    "lactosefree",
                    "lactose"
                )
            )

        val PROCESSING_STATE_GROUPS =
            listOf(
                setOf(
                    "raw",
                    "cooked",
                    "fried",
                    "roasted",
                    "baked"
                ),
                setOf(
                    "fresh",
                    "frozen",
                    "dried",
                    "canned",
                    "smoked",
                    "pickled",
                    "fermented"
                )
            )

        val NON_CRITICAL_PREPARATION_TOKENS =
            setOf(
                "ready",
                "prepared",
                "plain",
                "organic",
                "fresh",
                "creamy",
                "sliced",
                "ground",
                "whole"
            )

        val NON_CORE_TOKENS =
            NON_CRITICAL_PREPARATION_TOKENS +
                    setOf(
                        "lowfat",
                        "light",
                        "dark",
                        "black",
                        "white",
                        "red",
                        "green",
                        "yellow",
                        "style",
                        "classic",
                        "original",
                        "with",
                        "and",
                        "the",
                        "of",
                        "for",
                        "lunch"
                    )

        val SPECIFIC_PREPARED_MEAL_TOKENS =
            setOf(
                "bolognese",
                "lasagna",
                "curry",
                "goulash",
                "stew",
                "casserole",
                "gratin",
                "paella",
                "fricassee",
                "risotto",
                "pizza",
                "pancake",
                "schnitzel",
                "meatball"
            )
    }
}