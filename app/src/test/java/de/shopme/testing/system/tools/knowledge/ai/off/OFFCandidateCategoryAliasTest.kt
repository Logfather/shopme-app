package de.shopme.testing.system.tools.knowledge.off

import de.shopme.tools.knowledge.off.extractor.OFFCandidateExtractor
import org.junit.Assert.assertFalse
import kotlin.test.Test
import kotlin.test.assertTrue

class OFFCandidateCategoryAliasTest {

    @Test
    fun extractsCategoryAliases() {

        val aliases =
            OFFCandidateExtractor()
                .extractCategoryAliasesForTest(
                    categories = "en:condiments,en:mustards"
                )

        assertTrue(
            aliases.contains("mustard")
        )
    }

    @Test
    fun filtersBroadCategoryAliases() {

        val aliases =
            OFFCandidateExtractor()
                .extractCategoryAliasesForTest(
                    categories = "en:produce,en:meats,en:mustards,en:undefined,en:groceries"
                )

        assertFalse(
            aliases.contains("produce")
        )

        assertFalse(
            aliases.contains("meat")
        )

        assertFalse(
            aliases.contains("undefined")
        )

        assertFalse(
            aliases.contains("grocerie")
        )

        assertTrue(
            aliases.contains("mustard")
        )
    }

    @Test
    fun keepsOnlyTerminalCategoryAliases() {

        val aliases =
            OFFCandidateExtractor()
                .extractCategoryAliasesForTest(
                    categories =
                        "en:plant-based-foods," +
                                "en:vegetables," +
                                "en:leaf-vegetables," +
                                "en:broccolis"
                )

        assertFalse(
            aliases.contains("plant based")
        )

        assertFalse(
            aliases.contains("vegetable")
        )

        assertTrue(
            aliases.contains("leaf vegetable")
        )

        assertTrue(
            aliases.contains("broccoli")
        )
    }
}