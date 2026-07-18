package de.shopme.tools.knowledge.mapping.catalog

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.ai.AIProviderResponse

class DefaultCatalogKnowledgeMatchResponseParser :
    CatalogKnowledgeMatchResponseParser {

    override fun parse(
        request: CatalogKnowledgeMatchRequest,
        response: AIProviderResponse
    ): CatalogKnowledgeMatchDecision {

        val json =
            parseResponseObject(
                content = response.content
            )

        val isMatch =
            json.requiredBoolean(
                key = "match"
            )

        val confidence =
            json.requiredDouble(
                key = "confidence"
            )

        val reason =
            json.requiredString(
                key = "reason"
            )

        val selectedServerKey =
            json.optionalString(
                key = "selectedServerKey"
            )

        return CatalogKnowledgeMatchDecision(
            catalogKey = request.catalogKey,
            serverArtifact = request.serverArtifact,
            type =
                if (isMatch) {
                    CatalogKnowledgeMatchDecisionType.MATCH
                } else {
                    CatalogKnowledgeMatchDecisionType.NO_MATCH
                },
            selectedServerKey =
                if (isMatch) {
                    selectedServerKey
                } else {
                    null
                },
            confidence = confidence,
            reason = reason
        )
    }


    private fun parseResponseObject(
        content: String
    ): JsonObject {

        require(content.isNotBlank()) {
            "AI response content must not be blank"
        }

        val normalizedContent =
            content
                .trim()
                .removePrefix("```json")
                .removePrefix("```")
                .removeSuffix("```")
                .trim()

        val element =
            runCatching {
                JsonParser.parseString(
                    normalizedContent
                )
            }.getOrElse { cause ->
                throw IllegalArgumentException(
                    "AI response is not valid JSON",
                    cause
                )
            }

        require(element.isJsonObject) {
            "AI response must be a JSON object"
        }

        return element.asJsonObject
    }


    private fun JsonObject.requiredBoolean(
        key: String
    ): Boolean {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isBoolean
        ) {
            "Missing boolean '$key'"
        }

        return element.asBoolean
    }


    private fun JsonObject.requiredDouble(
        key: String
    ): Double {

        val element =
            get(key)

        require(
            element != null &&
                    !element.isJsonNull &&
                    element.isJsonPrimitive &&
                    element.asJsonPrimitive.isNumber
        ) {
            "Missing numeric '$key'"
        }

        return element.asDouble
    }


    private fun JsonObject.requiredString(
        key: String
    ): String {

        val value =
            optionalString(
                key = key
            )

        require(!value.isNullOrBlank()) {
            "Missing or blank '$key'"
        }

        return value
    }


    private fun JsonObject.optionalString(
        key: String
    ): String? =
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive &&
                        it.asJsonPrimitive.isString
            }
            ?.asString
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }
}