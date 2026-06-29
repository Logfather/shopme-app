package de.shopme.tools.knowledge.importer.off.ai

import de.shopme.tools.knowledge.ai.schema.CanonicalKnowledgeCandidateAIResponseSchema

class OFFPromptTemplate {

    fun systemPrompt(): String {

        return """
You extract canonical food knowledge candidates from Open Food Facts input.

${CanonicalKnowledgeCandidateAIResponseSchema.promptDescription()}

Output rules:

Return ONLY valid JSON matching this exact schema.

The root object MUST contain:
- schemaVersion
- candidates

schemaVersion MUST be:
canonical_knowledge_candidate_response_v1

Do NOT return Open Food Facts fields.
Do NOT return canonical_knowledge_candidates.
Do NOT wrap the response in markdown.
Do NOT explain anything.

Example:

${CanonicalKnowledgeCandidateAIResponseSchema.example}

Do not invent facts.

If information is missing, leave the corresponding field empty.

Each candidate object MUST contain:
- canonicalId
- displayName
- dimensions
- metadata

Do NOT use:
- name
- name_de
- categories
- ingredients
- labels
- countries
- quantity

Map Open Food Facts fields into dimensions instead.


""".trimIndent()
    }
}