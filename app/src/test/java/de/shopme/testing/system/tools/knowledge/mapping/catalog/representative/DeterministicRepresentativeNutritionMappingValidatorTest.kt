package de.shopme.testing.system.tools.knowledge.mapping.catalog.representative

import de.shopme.tools.knowledge.mapping.catalog.representative.DeterministicRepresentativeNutritionMappingValidator
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingReason
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingRequest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeterministicRepresentativeNutritionMappingValidatorTest {

    private val validator =
        DeterministicRepresentativeNutritionMappingValidator()

    @Test
    fun acceptIdenticalNormalizedMapping() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "Cherry Fruit Yogurt",
                        serverKey =
                            " cherry-fruit yogurt "
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType.IDENTICAL,
            actual =
                decision.type
        )

        assertEquals(
            expected =
                listOf(
                    RepresentativeNutritionMappingReason
                        .EXACT_NORMALIZED_KEY
                ),
            actual =
                decision.reasons
        )

        assertTrue(
            decision.accepted
        )
    }

    @Test
    fun acceptCherryYogurtAsRepresentative() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "cherry fruit yogurt",
                        serverKey =
                            "lowfat black cherry yogurt",
                        sharedTokens =
                            listOf(
                                "cherry",
                                "yogurt"
                            )
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .REPRESENTATIVE,
            actual =
                decision.type
        )

        assertTrue(
            RepresentativeNutritionMappingReason.SAME_PRODUCT_CLASS in
                    decision.reasons
        )

        assertTrue(
            RepresentativeNutritionMappingReason
                .COMPATIBLE_SPECIALIZATION in
                    decision.reasons
        )

        assertTrue(
            decision.accepted
        )
    }

    @Test
    fun acceptSpecificVeganReadyMealAsRepresentative() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "vegan ready meal",
                        serverKey =
                            "vegan spaghetti bolognese",
                        sharedTokens =
                            listOf(
                                "vegan"
                            )
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .REPRESENTATIVE,
            actual =
                decision.type
        )

        assertTrue(
            RepresentativeNutritionMappingReason.SAME_PRODUCT_CLASS in
                    decision.reasons
        )

        assertTrue(
            RepresentativeNutritionMappingReason
                .COMPATIBLE_SPECIALIZATION in
                    decision.reasons
        )
    }

    @Test
    fun acceptVegetarianWrapVariant() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "vegetarian wraps",
                        serverKey =
                            "vegetarian lettuce wraps lunch",
                        sharedTokens =
                            listOf(
                                "vegetarian",
                                "wraps"
                            )
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .REPRESENTATIVE,
            actual =
                decision.type
        )

        assertTrue(
            RepresentativeNutritionMappingReason.SAME_PRODUCT_CLASS in
                    decision.reasons
        )
    }

    @Test
    fun acceptHeadLettuceAsRepresentativeOfLettuce() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "head lettuce",
                        serverKey =
                            "lettuce",
                        sharedTokens =
                            listOf(
                                "lettuce"
                            )
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .REPRESENTATIVE,
            actual =
                decision.type
        )

        assertTrue(
            RepresentativeNutritionMappingReason
                .COMPATIBLE_SPECIALIZATION in
                    decision.reasons
        )
    }

    @Test
    fun rejectDrinkToYogurtProductFormConflict() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "soy drink",
                        serverKey =
                            "soy yogurt",
                        sharedTokens =
                            listOf(
                                "soy"
                            )
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .INCOMPATIBLE,
            actual =
                decision.type
        )

        assertTrue(
            RepresentativeNutritionMappingReason
                .PRODUCT_FORM_CONFLICT in
                    decision.reasons
        )

        assertFalse(
            decision.accepted
        )
    }

    @Test
    fun rejectFreshToDriedProcessingConflict() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "fresh parsley",
                        serverKey =
                            "dried parsley",
                        sharedTokens =
                            listOf(
                                "parsley"
                            )
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .INCOMPATIBLE,
            actual =
                decision.type
        )

        assertTrue(
            RepresentativeNutritionMappingReason
                .PROCESSING_STATE_CONFLICT in
                    decision.reasons
        )
    }

    @Test
    fun rejectUnsweetenedToSweetenedModifierConflict() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "unsweetened oat drink",
                        serverKey =
                            "sweetened oat drink",
                        sharedTokens =
                            listOf(
                                "oat",
                                "drink"
                            )
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .INCOMPATIBLE,
            actual =
                decision.type
        )

        assertTrue(
            RepresentativeNutritionMappingReason
                .CRITICAL_MODIFIER_CONFLICT in
                    decision.reasons
        )
    }

    @Test
    fun rejectUnrelatedProductsWithInsufficientEvidence() {

        val decision =
            validator.validate(
                request =
                    request(
                        catalogKey =
                            "apple juice",
                        serverKey =
                            "beef sausage",
                        sharedTokens =
                            emptyList()
                    )
            )

        assertEquals(
            expected =
                RepresentativeNutritionMappingDecisionType
                    .INCOMPATIBLE,
            actual =
                decision.type
        )

        assertFalse(
            decision.accepted
        )
    }

    @Test
    fun validateRequestsDeterministically() {

        val result =
            validator.validate(
                requests =
                    listOf(
                        request(
                            catalogKey =
                                "vegetarian wraps",
                            serverKey =
                                "vegetarian lettuce wraps lunch",
                            sharedTokens =
                                listOf(
                                    "vegetarian",
                                    "wraps"
                                )
                        ),
                        request(
                            catalogKey =
                                "cherry fruit yogurt",
                            serverKey =
                                "lowfat black cherry yogurt",
                            sharedTokens =
                                listOf(
                                    "cherry",
                                    "yogurt"
                                )
                        ),
                        request(
                            catalogKey =
                                "soy drink",
                            serverKey =
                                "soy yogurt",
                            sharedTokens =
                                listOf(
                                    "soy"
                                )
                        )
                    )
            )

        assertEquals(
            expected =
                listOf(
                    "cherry fruit yogurt",
                    "soy drink",
                    "vegetarian wraps"
                ),
            actual =
                result.decisions.map {
                    it.catalogKey
                }
        )

        assertEquals(
            expected = 2,
            actual =
                result.acceptedCount
        )

        assertEquals(
            expected = 1,
            actual =
                result.rejectedCount
        )
    }

    private fun request(
        catalogKey: String,
        serverKey: String,
        confidence: Double = 0.78,
        candidateRank: Int = 1,
        diagnosticScore: Double = 0.70,
        sharedTokens: List<String> = emptyList()
    ): RepresentativeNutritionMappingRequest =
        RepresentativeNutritionMappingRequest(
            catalogKey =
                catalogKey,
            serverKey =
                serverKey,
            confidence =
                confidence,
            candidateRank =
                candidateRank,
            diagnosticScore =
                diagnosticScore,
            sharedTokens =
                sharedTokens
        )
}