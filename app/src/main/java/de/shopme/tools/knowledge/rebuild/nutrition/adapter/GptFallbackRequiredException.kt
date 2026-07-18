package de.shopme.tools.knowledge.rebuild.nutrition.adapter

class GptFallbackRequiredException(
    val catalogKey: String
) : IllegalStateException(
    "ChatGPT fallback required for catalog key: $catalogKey"
)