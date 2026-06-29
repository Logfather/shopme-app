package de.shopme.testing.system.tools.knowledge.agribalyse

import de.shopme.tools.knowledge.agribalyse.mapper.AgribalyseReferenceMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgribalyseReferenceMapperTest {

    @Test
    fun mapsKnownReference() {

        val mapper =
            AgribalyseReferenceMapper(
                mapOf(
                    "pomme, pulpe, crue" to "apple"
                )
            )

        val result =
            mapper.map(
                "pomme, pulpe, crue"
            )

        assertEquals(
            "apple",
            result.reference
        )

        assertTrue(
            result.mapped
        )
    }

    @Test
    fun returnsOriginalReferenceWhenUnknown() {

        val mapper =
            AgribalyseReferenceMapper(
                emptyMap()
            )

        val result =
            mapper.map(
                "unknown product"
            )

        assertEquals(
            "unknown product",
            result.reference
        )

        assertFalse(
            result.mapped
        )
    }

    @Test
    fun normalizesCaseBeforeLookup() {

        val mapper =
            AgribalyseReferenceMapper(
                mapOf(
                    "pomme, pulpe, crue" to "apple"
                )
            )

        val result =
            mapper.map(
                "POMME, PULPE, CRUE"
            )

        assertEquals(
            "apple",
            result.reference
        )

        assertTrue(
            result.mapped
        )
    }

    @Test
    fun mapsBanana() {

        val mapper =
            AgribalyseReferenceMapper(
                mapOf(
                    "banane, pulpe, crue" to "banana"
                )
            )

        val result =
            mapper.map(
                "banane, pulpe, crue"
            )

        assertEquals(
            "banana",
            result.reference
        )

        assertTrue(
            result.mapped
        )
    }

    @Test
    fun mapsMilk() {

        val mapper =
            AgribalyseReferenceMapper(
                mapOf(
                    "lait demi-écrémé, uht" to "milk"
                )
            )

        val result =
            mapper.map(
                "lait demi-écrémé, uht"
            )

        assertEquals(
            "milk",
            result.reference
        )

        assertTrue(
            result.mapped
        )
    }

    @Test
    fun mapsBread() {

        val mapper =
            AgribalyseReferenceMapper(
                mapOf(
                    "pain courant français" to "bread"
                )
            )

        val result =
            mapper.map(
                "pain courant français"
            )

        assertEquals(
            "bread",
            result.reference
        )

        assertTrue(
            result.mapped
        )
    }

    @Test
    fun normalizesAccentsBeforeLookup() {

        val mapper =
            AgribalyseReferenceMapper(
                mapOf(
                    "epinard, cru" to "spinach"
                )
            )

        val result =
            mapper.map(
                "épinard, cru"
            )

        assertEquals(
            "spinach",
            result.reference
        )

        assertTrue(
            result.mapped
        )
    }

    @Test
    fun normalizesWhitespaceBeforeLookup() {

        val mapper =
            AgribalyseReferenceMapper(
                mapOf(
                    "pomme, pulpe, crue" to "apple"
                )
            )

        val result =
            mapper.map(
                "  pomme,   pulpe,   crue  "
            )

        assertEquals(
            "apple",
            result.reference
        )

        assertTrue(
            result.mapped
        )
    }

    @Test
    fun returnsNormalizedReferenceWhenUnknown() {

        val mapper =
            AgribalyseReferenceMapper(
                emptyMap()
            )

        val result =
            mapper.map(
                "  Épinard,   Cru  "
            )

        assertEquals(
            "epinard, cru",
            result.reference
        )

        assertFalse(
            result.mapped
        )
    }
}