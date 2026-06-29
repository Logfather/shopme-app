package de.shopme.testing.system.tools.knowledge.importer.off.ai

import com.google.gson.JsonSyntaxException
import de.shopme.tools.knowledge.ai.AIProviderResponse
import de.shopme.tools.knowledge.ai.JsonAIProviderResponseParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AIResponseParserMalformedJsonTest {

    @Test
    fun parseRejectsMalformedJson() {

        val parser = JsonAIProviderResponseParser()

        try {

            parser.parse(
                AIProviderResponse(
                    content = """
                        {
                          "schemaVersion":
                    """.trimIndent()
                )
            )

            fail("Expected IllegalArgumentException.")

        } catch (exception: IllegalArgumentException) {

            assertEquals(
                "Invalid AI response JSON.",
                exception.message
            )

            assertTrue(
                exception.cause is JsonSyntaxException
            )
        }
    }
}