package de.shopme.testing.system.tools.knowledge.nutrition.training

import com.google.gson.JsonParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NutritionDomainMismatchFeatureExtractorTest {

    private val extractor =
        NutritionDomainMismatchFeatureExtractor()

    @Test
    fun extractAggregatedDomainMismatchFeatures() {
        val mismatchEntry =
            JsonParser.parseString(
                """
                {
                  "catalogKey": "canned chanterelles",
                  "serverKey": "canned beef",
                  "rank": 2,
                  "primaryMismatchType": "CROSS_DOMAIN_MISMATCH",
                  "observations": [
                    {
                      "mismatchType": "CROSS_DOMAIN_MISMATCH"
                    },
                    {
                      "mismatchType": "FORM_OR_PROCESSING_DIFFERENCE"
                    },
                    {
                      "mismatchType": "UNKNOWN_TOKEN_INVOLVED"
                    }
                  ]
                }
                """.trimIndent(),
            ).asJsonObject

        val result =
            extractor.extract(
                mismatchEntry = mismatchEntry,
            )

        assertTrue(
            result.reportRelationshipPresent,
        )

        assertEquals(
            "CROSS_DOMAIN_MISMATCH",
            result.primaryMismatchType,
        )

        assertEquals(
            3,
            result.observationCount,
        )

        assertEquals(
            1,
            result.crossDomainMismatchCount,
        )

        assertEquals(
            1,
            result.formOrProcessingDifferenceCount,
        )

        assertEquals(
            1,
            result.unknownTokenInvolvedCount,
        )

        assertEquals(
            1,
            result.identityConflictCount,
        )

        assertEquals(
            1,
            result.modifierDifferenceCount,
        )

        assertEquals(
            2,
            result.knownSemanticObservationCount,
        )

        assertEquals(
            1,
            result.unknownSemanticObservationCount,
        )
    }

    @Test
    fun missingRelationshipProducesZeroFeatures() {
        val result =
            extractor.extract(
                mismatchEntry = null,
            )

        assertFalse(
            result.reportRelationshipPresent,
        )

        assertEquals(
            null,
            result.primaryMismatchType,
        )

        assertEquals(
            0,
            result.observationCount,
        )

        assertEquals(
            0,
            result.identityConflictCount,
        )

        assertEquals(
            0,
            result.modifierDifferenceCount,
        )
    }
}