package de.shopme.tools.report

import android.content.Context
import com.google.gson.JsonObject
import com.google.gson.JsonParser

class RuntimeCatalogKnowledgeKeyCounter(
    private val context: Context
) {

    fun count(): Int =
        keys().size


    fun keys(): Set<String> =
        readCatalogItems()
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


    private fun readCatalogItems(): List<JsonObject> {

        val json =
            context.assets
                .open(
                    CATALOG_ASSET
                )
                .bufferedReader()
                .use {
                    it.readText()
                }

        val root =
            JsonParser
                .parseString(json)

        val array =
            when {
                root.isJsonArray ->
                    root.asJsonArray

                root.isJsonObject ->
                    root.asJsonObject
                        .firstArray(
                            "items",
                            "products",
                            "entries",
                            "catalog"
                        )
                        ?: error(
                            "Unsupported catalog structure"
                        )

                else ->
                    error(
                        "Unsupported catalog JSON"
                    )
            }

        return array
            .mapNotNull {
                it.takeIf { element ->
                    element.isJsonObject
                }?.asJsonObject
            }
    }


    private fun JsonObject.firstArray(
        vararg keys: String
    ) =
        keys
            .firstNotNullOfOrNull { key ->

                val value =
                    get(key)

                if (
                    value != null &&
                    value.isJsonArray
                ) {
                    value.asJsonArray
                } else {
                    null
                }
            }


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

        var previousWhitespace =
            false

        for (char in this) {

            if (char.isWhitespace()) {

                if (!previousWhitespace) {
                    result.append(' ')
                }

                previousWhitespace =
                    true

            } else {

                result.append(char)

                previousWhitespace =
                    false
            }
        }

        return result.toString()
    }


    companion object {

        private const val CATALOG_ASSET =
            "catalog/catalog.json"
    }
}