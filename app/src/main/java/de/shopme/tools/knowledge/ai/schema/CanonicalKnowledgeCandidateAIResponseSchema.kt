package de.shopme.tools.knowledge.ai.schema

object CanonicalKnowledgeCandidateAIResponseSchema {

    const val VERSION =
        "canonical_knowledge_candidate_response_v1"

    fun promptDescription(): String {

        return """
Schema version:
$VERSION

Return JSON only.

The JSON must conform to the schema.

Do not add explanations.

Do not add markdown.

Do not wrap the JSON in code fences.

Use the defined response structure.
""".trimIndent()
    }

    val example = """
    ...
    """.trimIndent()
}