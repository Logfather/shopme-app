package de.shopme.testing.system.tools.knowledge.mapping.catalog.representative

import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecision
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingDecisionType
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingReason
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingRequest
import de.shopme.tools.knowledge.mapping.catalog.representative.RepresentativeNutritionMappingValidationResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RepresentativeNutritionMappingContractTest {

    @Test
    fun acceptIdenticalNutritionMapping() {

        val decision =
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "dried peppermint",
                serverKey =
                    "dried peppermint",
                type =
                    RepresentativeNutritionMappingDecisionType.IDENTICAL,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .EXACT_NORMALIZED_KEY
                    )
            )

        assertTrue(
            actual = decision.accepted
        )
    }

    @Test
    fun acceptRepresentativeNutritionMapping() {

        val decision =
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "cherry fruit yogurt",
                serverKey =
                    "lowfat black cherry yogurt",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS,
                        RepresentativeNutritionMappingReason
                            .COMPATIBLE_SPECIALIZATION,
                        RepresentativeNutritionMappingReason
                            .COMPATIBLE_VARIANT
                    )
            )

        assertTrue(
            actual = decision.accepted
        )
    }

    @Test
    fun rejectIncompatibleNutritionMapping() {

        val decision =
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "soy drink",
                serverKey =
                    "soy yogurt",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .INCOMPATIBLE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .PRODUCT_FORM_CONFLICT
                    )
            )

        assertFalse(
            actual = decision.accepted
        )
    }

    @Test
    fun normalizeRepresentativeNutritionMappingRequest() {

        val request =
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    "  Cherry Fruit Yogurt  ",
                serverKey =
                    "  Lowfat Black Cherry Yogurt  ",
                confidence =
                    0.78,
                candidateRank =
                    2,
                diagnosticScore =
                    0.74,
                sharedTokens =
                    listOf(
                        " yogurt ",
                        "cherry",
                        "yogurt",
                        "",
                        " fruit "
                    )
            )

        val normalized =
            request.normalized()

        assertEquals(
            expected =
                "Cherry Fruit Yogurt",
            actual =
                normalized.catalogKey
        )

        assertEquals(
            expected =
                "Lowfat Black Cherry Yogurt",
            actual =
                normalized.serverKey
        )

        assertEquals(
            expected =
                listOf(
                    "cherry",
                    "fruit",
                    "yogurt"
                ),
            actual =
                normalized.sharedTokens
        )
    }

    @Test
    fun rejectBlankCatalogKey() {

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    " ",
                serverKey =
                    "lettuce",
                confidence =
                    0.78,
                candidateRank =
                    1
            )
        }
    }

    @Test
    fun rejectBlankServerKey() {

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    "head lettuce",
                serverKey =
                    " ",
                confidence =
                    0.78,
                candidateRank =
                    1
            )
        }
    }

    @Test
    fun rejectInvalidConfidence() {

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    "head lettuce",
                serverKey =
                    "lettuce",
                confidence =
                    1.01,
                candidateRank =
                    1
            )
        }

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    "head lettuce",
                serverKey =
                    "lettuce",
                confidence =
                    -0.01,
                candidateRank =
                    1
            )
        }
    }

    @Test
    fun rejectInvalidCandidateRank() {

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    "head lettuce",
                serverKey =
                    "lettuce",
                confidence =
                    0.78,
                candidateRank =
                    0
            )
        }
    }

    @Test
    fun rejectInvalidDiagnosticScore() {

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingRequest(
                catalogKey =
                    "head lettuce",
                serverKey =
                    "lettuce",
                confidence =
                    0.78,
                candidateRank =
                    1,
                diagnosticScore =
                    1.1
            )
        }
    }

    @Test
    fun requireDeterministicallySortedReasons() {

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "cherry fruit yogurt",
                serverKey =
                    "lowfat black cherry yogurt",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .COMPATIBLE_VARIANT,
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS
                    )
            )
        }
    }

    @Test
    fun rejectDuplicateReasons() {

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "cherry fruit yogurt",
                serverKey =
                    "lowfat black cherry yogurt",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS,
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS
                    )
            )
        }
    }

    @Test
    fun requireExactlyOneDecisionPerCatalogKey() {

        val first =
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "head lettuce",
                serverKey =
                    "lettuce",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS
                    )
            )

        val second =
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    " head lettuce ",
                serverKey =
                    "green lettuce",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS
                    )
            )

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingValidationResult(
                decisions =
                    listOf(
                        first,
                        second
                    )
            )
        }
    }

    @Test
    fun requireDeterministicallySortedDecisions() {

        val apple =
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "apple",
                serverKey =
                    "apple fresh",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS
                    )
            )

        val banana =
            RepresentativeNutritionMappingDecision(
                catalogKey =
                    "banana",
                serverKey =
                    "banana raw",
                type =
                    RepresentativeNutritionMappingDecisionType
                        .REPRESENTATIVE,
                reasons =
                    listOf(
                        RepresentativeNutritionMappingReason
                            .SAME_PRODUCT_CLASS
                    )
            )

        assertFailsWith<IllegalArgumentException> {
            RepresentativeNutritionMappingValidationResult(
                decisions =
                    listOf(
                        banana,
                        apple
                    )
            )
        }

        val result =
            RepresentativeNutritionMappingValidationResult(
                decisions =
                    listOf(
                        apple,
                        banana
                    )
            )

        assertEquals(
            expected = 2,
            actual =
                result.acceptedCount
        )

        assertEquals(
            expected = 0,
            actual =
                result.rejectedCount
        )
    }

    @Test
    fun separateAcceptedAndRejectedDecisions() {

        val decisions =
            listOf(
                RepresentativeNutritionMappingDecision(
                    catalogKey =
                        "cherry fruit yogurt",
                    serverKey =
                        "lowfat black cherry yogurt",
                    type =
                        RepresentativeNutritionMappingDecisionType
                            .REPRESENTATIVE,
                    reasons =
                        listOf(
                            RepresentativeNutritionMappingReason
                                .SAME_PRODUCT_CLASS
                        )
                ),
                RepresentativeNutritionMappingDecision(
                    catalogKey =
                        "soy drink",
                    serverKey =
                        "soy yogurt",
                    type =
                        RepresentativeNutritionMappingDecisionType
                            .INCOMPATIBLE,
                    reasons =
                        listOf(
                            RepresentativeNutritionMappingReason
                                .PRODUCT_FORM_CONFLICT
                        )
                )
            )

        val result =
            RepresentativeNutritionMappingValidationResult(
                decisions =
                    decisions
            )

        assertEquals(
            expected = 1,
            actual =
                result.acceptedCount
        )

        assertEquals(
            expected = 1,
            actual =
                result.rejectedCount
        )

        assertEquals(
            expected =
                "cherry fruit yogurt",
            actual =
                result.acceptedDecisions
                    .single()
                    .catalogKey
        )

        assertEquals(
            expected =
                "soy drink",
            actual =
                result.rejectedDecisions
                    .single()
                    .catalogKey
        )
    }
}