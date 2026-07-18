package de.shopme.tools.knowledge.compiler.catalog

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class CatalogItemMerger {

    fun merge(
        primary: JsonObject,
        incoming: JsonObject
    ): JsonObject {
        val result = primary.deepCopy()

        incoming.entrySet().forEach { (key, incomingValue) ->
            val existingValue = result.get(key)

            result.add(
                key,
                mergeValue(
                    key = key,
                    primary = existingValue,
                    incoming = incomingValue
                )
            )
        }

        return result
    }

    private fun mergeValue(
        key: String,
        primary: JsonElement?,
        incoming: JsonElement
    ): JsonElement {
        if (primary == null || primary.isJsonNull) {
            return incoming.deepCopy()
        }

        if (isCollectionKey(key) && primary.isJsonArray && incoming.isJsonArray) {
            return mergeArrays(
                primary = primary.asJsonArray,
                incoming = incoming.asJsonArray
            )
        }

        if (key == "knowledge" && primary.isJsonObject && incoming.isJsonObject) {
            return mergeObjects(
                primary = primary.asJsonObject,
                incoming = incoming.asJsonObject
            )
        }

        if (primary.isJsonObject && incoming.isJsonObject) {
            return mergeObjects(
                primary = primary.asJsonObject,
                incoming = incoming.asJsonObject
            )
        }

        return primary.deepCopy()
    }

    private fun mergeObjects(
        primary: JsonObject,
        incoming: JsonObject
    ): JsonObject {
        val result = primary.deepCopy()

        incoming.entrySet().forEach { (key, incomingValue) ->
            val existingValue = result.get(key)

            result.add(
                key,
                mergeValue(
                    key = key,
                    primary = existingValue,
                    incoming = incomingValue
                )
            )
        }

        return result
    }

    private fun mergeArrays(
        primary: JsonArray,
        incoming: JsonArray
    ): JsonArray {
        val result = JsonArray()
        val seen = linkedSetOf<String>()

        fun addUnique(value: JsonElement) {
            val key = value.toString()

            if (seen.add(key)) {
                result.add(value.deepCopy())
            }
        }

        primary.forEach(::addUnique)
        incoming.forEach(::addUnique)

        return result
    }

    private fun isCollectionKey(
        key: String
    ): Boolean {
        return key == "plural" ||
                key == "colloquial" ||
                key == "phonetic_tokens" ||
                key == "autocomplete_tokens"
    }
}