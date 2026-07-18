package de.shopme.tools.report

import com.google.gson.JsonObject

class CatalogKnowledgeKeyExtractor {

    fun extract(
        items: List<JsonObject>
    ): Set<String> =
        items
            .asSequence()
            .mapNotNull { item ->
                item.string("normalizedEnglish")
                    ?: item.string("name")
                    ?: item.string("productName")
                    ?: item.string("title")
            }
            .map {
                normalizeKey(it)
            }
            .filter {
                it.isNotBlank()
            }
            .toSortedSet()


    private fun JsonObject.string(
        key: String
    ): String? =
        get(key)
            ?.takeIf {
                !it.isJsonNull &&
                        it.isJsonPrimitive
            }
            ?.asString
            ?.trim()
            ?.takeIf {
                it.isNotBlank()
            }


    private fun normalizeKey(
        value: String
    ): String =
        value
            .trim()
            .lowercase()
            .replace("-", " ")
            .replace("_", " ")
            .collapseWhitespace()
            .trim()


    private fun String.collapseWhitespace(): String {

        val result =
            StringBuilder(length)

        var previousWhitespace = false

        for (char in this) {

            if (char.isWhitespace()) {

                if (!previousWhitespace) {
                    result.append(' ')
                }

                previousWhitespace = true

            } else {

                result.append(char)

                previousWhitespace = false
            }
        }

        return result.toString()
    }
}