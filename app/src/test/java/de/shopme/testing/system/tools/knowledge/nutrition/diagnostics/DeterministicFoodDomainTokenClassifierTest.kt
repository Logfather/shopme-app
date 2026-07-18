package de.shopme.testing.system.tools.knowledge.nutrition.diagnostics

import kotlin.test.Test
import kotlin.test.assertEquals

class DeterministicFoodDomainTokenClassifierTest {

    private val classifier =
        DeterministicFoodDomainTokenClassifier()

    @Test
    fun classifyRepresentativeFoodDomainTokens() {
        assertClassification(
            token = "123",
            expected = FoodDomainTokenClass.NUMERIC,
        )

        assertClassification(
            token = "with",
            expected = FoodDomainTokenClass.STOPWORD,
        )

        assertClassification(
            token = "chicken",
            expected = FoodDomainTokenClass.ANIMAL_SPECIES,
        )

        assertClassification(
            token = "liver",
            expected = FoodDomainTokenClass.ANIMAL_PRODUCT_OR_CUT,
        )

        assertClassification(
            token = "Jagdwurst",
            expected = FoodDomainTokenClass.PROCESSED_ANIMAL_PRODUCT,
        )

        assertClassification(
            token = "chanterelles",
            expected = FoodDomainTokenClass.PLANT_INGREDIENT,
        )

        assertClassification(
            token = "spelt",
            expected = FoodDomainTokenClass.GRAIN_OR_LEGUME,
        )

        assertClassification(
            token = "linseed",
            expected = FoodDomainTokenClass.NUT_SEED_OR_OIL_SOURCE,
        )

        assertClassification(
            token = "marjoram",
            expected = FoodDomainTokenClass.HERB_OR_SPICE,
        )

        assertClassification(
            token = "butter",
            expected = FoodDomainTokenClass.DAIRY_PRODUCT,
        )

        assertClassification(
            token = "fricassee",
            expected = FoodDomainTokenClass.DISH_OR_MEAL,
        )

        assertClassification(
            token = "noodles",
            expected = FoodDomainTokenClass.BAKERY_OR_STARCH_PRODUCT,
        )

        assertClassification(
            token = "beer",
            expected = FoodDomainTokenClass.BEVERAGE,
        )

        assertClassification(
            token = "chocolate",
            expected = FoodDomainTokenClass.SWEET_PRODUCT,
        )

        assertClassification(
            token = "kernels",
            expected = FoodDomainTokenClass.PRODUCT_FORM,
        )

        assertClassification(
            token = "diced",
            expected = FoodDomainTokenClass.PRODUCT_FORM,
        )

        assertClassification(
            token = "pickled",
            expected = FoodDomainTokenClass.PREPARATION_OR_PROCESSING,
        )

        assertClassification(
            token = "brown",
            expected = FoodDomainTokenClass.COLOR_OR_APPEARANCE,
        )

        assertClassification(
            token = "Franconian",
            expected = FoodDomainTokenClass.REGION_OR_CUISINE,
        )

        assertClassification(
            token = "farmhouse",
            expected = FoodDomainTokenClass.STYLE_OR_QUALITY_MODIFIER,
        )

        assertClassification(
            token = "mini",
            expected = FoodDomainTokenClass.QUANTITY_OR_SIZE_MODIFIER,
        )

        assertClassification(
            token = "tofu",
            expected = FoodDomainTokenClass.DIET_OR_SUBSTITUTE,
        )

        assertClassification(
            token = "vacuum",
            expected = FoodDomainTokenClass.PACKAGING_OR_PRESENTATION,
        )

        assertClassification(
            token = "appifizz",
            expected = FoodDomainTokenClass.UNKNOWN,
        )
    }

    @Test
    fun normalizePluralAndAccentVariantsDeterministically() {
        val mango =
            classifier.classify(
                token = "Mangoes",
            )

        assertEquals(
            expected = "mango",
            actual = mango.normalizedToken,
        )

        val acai =
            classifier.classify(
                token = "Açaí",
            )

        assertEquals(
            expected = "acai",
            actual = acai.normalizedToken,
        )

        assertEquals(
            expected = FoodDomainTokenClass.PLANT_INGREDIENT,
            actual = acai.tokenClass,
        )
    }

    private fun assertClassification(
        token: String,
        expected: FoodDomainTokenClass,
    ) {
        val actual =
            classifier.classify(
                token = token,
            )

        assertEquals(
            expected = expected,
            actual = actual.tokenClass,
            message =
                "Unexpected Food-Domain classification for token=$token, " +
                        "normalized=${actual.normalizedToken}",
        )
    }
}