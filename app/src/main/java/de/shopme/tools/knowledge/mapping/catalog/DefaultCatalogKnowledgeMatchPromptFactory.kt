package de.shopme.tools.knowledge.mapping.catalog

import de.shopme.tools.knowledge.ai.AIProviderRequest

class DefaultCatalogKnowledgeMatchPromptFactory :
    CatalogKnowledgeMatchPromptFactory {

    override fun create(
        request: CatalogKnowledgeMatchRequest
    ): AIProviderRequest {

        return AIProviderRequest(
            systemPrompt =
                createSystemPrompt(),
            userPrompt =
                createUserPrompt(
                    request = request
                )
        )
    }


    private fun createSystemPrompt(): String =
        """
You are a food identity matcher.

Decision rules:

1. Return MATCH only when one candidate clearly represents the same food.
2. Return NO_MATCH when none of the candidates represents the same food.
3. Never invent, rewrite, normalize, translate, or shorten a server key.
4. For MATCH, selectedServerKey must be copied exactly from one supplied candidate.
5. For NO_MATCH, selectedServerKey must be null.
6. diagnosticScore and sharedTokens are retrieval metadata only. They do not prove that the foods are identical.

Confidence semantics:

- confidence expresses confidence in the complete decision.
- For MATCH, confidence means certainty that the selected candidate represents the same food.
- For NO_MATCH, confidence means certainty that none of the supplied candidates is an acceptable match.
- A confident NO_MATCH must therefore use a high confidence value.
- Do not set confidence to 0 merely because no candidate was selected.

Return only one valid JSON object with exactly these fields:

{
  "match": true or false,
  "selectedServerKey": "exact supplied candidate key" or null,
  "confidence": number from 0.0 to 1.0,
  "reason": "brief factual explanation"
}

Do not return Markdown.
Do not return additional fields.
    """.trimIndent()


    private fun createUserPrompt(
        request: CatalogKnowledgeMatchRequest
    ): String =
        buildString {

            appendLine("Catalog food:")
            appendLine(request.catalogKey)
            appendLine()

            appendLine("Server artifact:")
            appendLine(request.serverArtifact)
            appendLine()

            appendLine("Candidates:")

            request.candidates
                .forEachIndexed { index, candidate ->

                    append(index + 1)
                    append(". serverKey=\"")
                    append(candidate.serverKey)
                    append("\"")
                    append(", diagnosticScore=")
                    append(candidate.diagnosticScore)
                    append(", sharedTokens=[")

                    append(
                        candidate.sharedTokens
                            .joinToString(
                                separator = ", "
                            ) { token ->
                                "\"$token\""
                            }
                    )

                    appendLine("]")
                }

            appendLine()
            appendLine(
                "Decide whether exactly one candidate represents " +
                        "the same food."
            )
        }
}