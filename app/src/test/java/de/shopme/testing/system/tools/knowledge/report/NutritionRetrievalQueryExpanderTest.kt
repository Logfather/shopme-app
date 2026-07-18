package de.shopme.testing.system.tools.knowledge.report

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NutritionRetrievalQueryExpanderTest {

    @Test
    fun expandsKnownMultilingualNutritionAliases() {

        val expander =
            NutritionRetrievalQueryExpander()

        assertEquals(
            expected =
                listOf(
                    "chervil",
                    "kerbel"
                ),
            actual =
                expander.expand(
                    catalogKey =
                        "chervil"
                )
        )

        val leberkaeseQueries =
            expander.expand(
                catalogKey =
                    "leberkaese"
            )

        assertEquals(
            expected =
                "leberkaese",
            actual =
                leberkaeseQueries.first()
        )

        assertTrue(
            "leberkase" in
                    leberkaeseQueries
        )

        assertTrue(
            "bavarian meat loaf" in
                    leberkaeseQueries
        )

        assertTrue(
            "meatloaf" in
                    leberkaeseQueries
        )

        assertEquals(
            expected =
                leberkaeseQueries.distinct(),
            actual =
                leberkaeseQueries
        )
    }

    @Test
    fun keepsUnknownCatalogKeyAsOnlyQuery() {

        val expander =
            NutritionRetrievalQueryExpander()

        assertEquals(
            expected =
                listOf(
                    "ordinary apple"
                ),
            actual =
                expander.expand(
                    catalogKey =
                        "ordinary apple"
                )
        )
    }

    @Test
    fun normalizesDiacriticsAndWhitespaceDeterministically() {

        val registry =
            NutritionRetrievalAliasRegistry(
                aliases =
                    mapOf(
                        "  Muskatblüte  " to
                                setOf(
                                    " Mace   Spice ",
                                    "mace spice"
                                )
                    )
            )

        assertEquals(
            expected =
                listOf(
                    "mace spice"
                ),
            actual =
                registry.aliasesFor(
                    catalogKey =
                        "muskatbluete"
                )
        )
    }
}