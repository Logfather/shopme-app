package de.shopme.testing.system.speechTest

import de.shopme.data.input.speech.SpeechItemParser
import de.shopme.domain.catalog.CatalogIndex
import de.shopme.domain.catalog.CatalogItem
import de.shopme.domain.service.CatalogService
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SpeechItemParserTest {

    private lateinit var parser: SpeechItemParser

    @Before
    fun setup() {

        val items = listOf(

            CatalogItem(
                itemname = "Milch",
                category = "Molkerei",
                production = "",
                normalized = "milch",
                plural = "Milch",
                colloquial = emptyList(),
                phonetic_tokens = listOf("milch"),
                autocomplete_tokens = listOf("mil")
            ),

            CatalogItem(
                itemname = "Butter",
                category = "Molkerei",
                production = "",
                normalized = "butter",
                plural = "Butter",
                colloquial = emptyList(),
                phonetic_tokens = listOf("butter"),
                autocomplete_tokens = listOf("but")
            ),

            CatalogItem(
                itemname = "Vollmilch",
                category = "Molkerei",
                production = "",
                normalized = "vollmilch",
                plural = "Vollmilch",
                colloquial = emptyList(),
                phonetic_tokens = listOf("vollmilch"),
                autocomplete_tokens = listOf("voll")
            )
        )

        val catalogService =
            CatalogService(
                CatalogIndex(items)
            )

        parser =
            SpeechItemParser(
                catalogService
            )
    }

    @Test
    fun parses_single_item() {

        val result =
            parser.parseSpeech("milch")

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals(1, result[0].quantity)
    }

    @Test
    fun parses_numeric_quantity() {

        val result =
            parser.parseSpeech("2 milch")

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals(2, result[0].quantity)
    }

    @Test
    fun parses_number_word_quantity() {

        val result =
            parser.parseSpeech("zwei milch")

        Assert.assertEquals(1, result.size)
        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals(2, result[0].quantity)
    }

    @Test
    fun parses_multiple_items() {

        val result =
            parser.parseSpeech("milch butter")

        Assert.assertEquals(2, result.size)

        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals("Butter", result[1].name)
    }

    @Test
    fun parses_and_separator() {

        val result =
            parser.parseSpeech("milch und butter")

        Assert.assertEquals(2, result.size)

        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals("Butter", result[1].name)
    }

    @Test
    fun parses_multiple_quantities() {

        val result =
            parser.parseSpeech("2 milch 3 butter")

        Assert.assertEquals(2, result.size)

        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals(2, result[0].quantity)

        Assert.assertEquals("Butter", result[1].name)
        Assert.assertEquals(3, result[1].quantity)
    }

    @Test
    fun aggregates_duplicate_items() {

        val result =
            parser.parseSpeech("milch milch")

        Assert.assertEquals(1, result.size)

        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals(2, result[0].quantity)
    }

    @Test
    fun aggregates_duplicate_items_with_quantities() {

        val result =
            parser.parseSpeech("2 milch milch")

        Assert.assertEquals(1, result.size)

        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals(3, result[0].quantity)
    }

    @Test
    fun parses_compound_word() {

        val result =
            parser.parseSpeech("milchbutter")

        Assert.assertEquals(2, result.size)

        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals("Butter", result[1].name)
    }

    @Test
    fun parses_quantity_compound_word() {

        val result =
            parser.parseSpeech("2 milchbutter")

        Assert.assertEquals(2, result.size)

        Assert.assertEquals("Milch", result[0].name)
        Assert.assertEquals(2, result[0].quantity)

        Assert.assertEquals("Butter", result[1].name)
        Assert.assertEquals(2, result[1].quantity)
    }
}