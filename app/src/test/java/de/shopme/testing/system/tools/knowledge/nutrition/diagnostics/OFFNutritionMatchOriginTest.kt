package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.OFFNutritionMatchOrigin
import de.shopme.tools.knowledge.rebuild.nutrition.diagnostics.OFFNutritionMatchOriginClassifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class OFFNutritionMatchOriginTest {

    private val classifier =
        OFFNutritionMatchOriginClassifier()

    @Test
    fun classifiesDirectMaceProductBeforeIngredientText() {

        val match =
            classifier.classify(
                aliases =
                    listOf(
                        "mace",
                        "ground mace",
                        "macis",
                        "muskatblüte"
                    ),
                directIdentityValues =
                    listOf(
                        "Organic Mace Ground"
                    ),
                ingredientIdentityValues =
                    listOf(
                        "organic mace"
                    )
            )

        requireNotNull(match)

        assertEquals(
            expected =
                OFFNutritionMatchOrigin
                    .DIRECT_PRODUCT_IDENTITY,
            actual =
                match.origin
        )
    }

    @Test
    fun classifiesSpiceCakeAsIngredientOnly() {

        val match =
            classifier.classify(
                aliases =
                    listOf(
                        "mace",
                        "ground mace",
                        "macis",
                        "muskatblüte"
                    ),
                directIdentityValues =
                    listOf(
                        "King Soopers Spice Cake"
                    ),
                ingredientIdentityValues =
                    listOf(
                        "water sugar flour cinnamon nutmeg mace maltol"
                    )
            )

        requireNotNull(match)

        assertEquals(
            expected =
                OFFNutritionMatchOrigin
                    .INGREDIENT_ONLY,
            actual =
                match.origin
        )

        assertEquals(
            expected =
                listOf(
                    "mace"
                ),
            actual =
                match.matchedAliases
        )
    }

    @Test
    fun prefersDirectMatchWhenBothOriginsMatch() {

        val match =
            classifier.classify(
                aliases =
                    listOf(
                        "chervil",
                        "kerbel"
                    ),
                directIdentityValues =
                    listOf(
                        "Kerbel gerebelt"
                    ),
                ingredientIdentityValues =
                    listOf(
                        "kerbel"
                    )
            )

        requireNotNull(match)

        assertEquals(
            expected =
                OFFNutritionMatchOrigin
                    .DIRECT_PRODUCT_IDENTITY,
            actual =
                match.origin
        )
    }

    @Test
    fun rejectsSubstringInsideLongerToken() {

        val match =
            classifier.classify(
                aliases =
                    listOf(
                        "mace"
                    ),
                directIdentityValues =
                    listOf(
                        "macedonian pastry"
                    ),
                ingredientIdentityValues =
                    emptyList()
            )

        assertNull(
            actual =
                match
        )
    }
}