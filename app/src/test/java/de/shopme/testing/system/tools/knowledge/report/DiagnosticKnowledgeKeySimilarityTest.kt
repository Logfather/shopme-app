package de.shopme.testing.system.tools.knowledge.report

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DiagnosticKnowledgeKeySimilarityTest {

    @Test
    fun scoresOverlappingKnowledgeKeys() {

        val result =
            DiagnosticKnowledgeKeySimilarity.score(
                catalogKey = "semi skimmed milk",
                serverKey = "milk semi skimmed uht"
            )

        assertEquals(
            listOf(
                "milk",
                "semi",
                "skimmed"
            ),
            result.sharedTokens
        )

        assertTrue(
            result.score > 0.70,
            "Expected strong diagnostic similarity but was ${result.score}"
        )
    }

    @Test
    fun returnsZeroWithoutSharedTokens() {

        val result =
            DiagnosticKnowledgeKeySimilarity.score(
                catalogKey = "elderflower syrup",
                serverKey = "beef steak grilled"
            )

        assertEquals(
            0.0,
            result.score
        )

        assertTrue(
            result.sharedTokens.isEmpty()
        )
    }
}