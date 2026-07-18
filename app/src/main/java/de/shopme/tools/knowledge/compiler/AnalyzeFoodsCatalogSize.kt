package de.shopme.tools.knowledge.compiler

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.stream.JsonReader
import java.io.File

object AnalyzeFoodsCatalogSize {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputFile =
            if (args.isNotEmpty()) {
                File(args[0])
            } else {
                File("../data/generated/foods.off.json")
            }

        val sampleSize =
            if (args.size >= 2) {
                args[1].toInt()
            } else {
                1_000
            }

        require(inputFile.exists()) {
            "Catalog not found: ${inputFile.absolutePath}"
        }

        val gson = Gson()

        var count = 0
        var totalBytes = 0L

        val fieldSizes =
            linkedMapOf<String, Long>()

        val largestItems =
            mutableListOf<ItemSize>()

        JsonReader(inputFile.reader()).use { reader ->
            reader.beginArray()

            while (reader.hasNext() && count < sampleSize) {
                val item =
                    gson.fromJson<JsonObject>(
                        reader,
                        JsonObject::class.java
                    )

                val itemJson =
                    item.toString()

                val itemBytes =
                    itemJson.toByteArray().size

                totalBytes += itemBytes
                count++

                item.entrySet().forEach { (key, value) ->
                    fieldSizes[key] =
                        fieldSizes.getOrDefault(key, 0L) +
                                value.toString().toByteArray().size
                }

                largestItems += ItemSize(
                    name = item.string("itemname")
                        ?: item.string("normalized")
                        ?: "<unknown>",
                    bytes = itemBytes,
                    fields = item.keySet().toList()
                )
            }
        }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 FOODS CATALOG SIZE ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("File        : ${inputFile.absolutePath}")
        println("File size   : ${inputFile.length() / 1024 / 1024} MB")
        println("Sample      : $count items")
        println("Avg item    : ${if (count == 0) 0 else totalBytes / count} bytes")
        println("Projected   : ${(totalBytes / count) * estimateTotalItems(inputFile, count)} bytes rough")
        println()
        println("Field sizes in sample:")
        fieldSizes
            .entries
            .sortedByDescending { it.value }
            .forEach { (field, bytes) ->
                println("  $field: $bytes bytes")
            }

        println()
        println("Largest sampled items:")
        largestItems
            .sortedByDescending { it.bytes }
            .take(10)
            .forEach {
                println("  ${it.bytes} bytes | ${it.name} | fields=${it.fields}")
            }

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun estimateTotalItems(
        file: File,
        sampleCount: Int
    ): Long {
        // Placeholder estimate intentionally simple.
        // Real count is expensive for multi-GB JSON arrays.
        return sampleCount.toLong()
    }

    private fun JsonObject.string(
        key: String
    ): String? {
        return get(key)
            ?.takeIf { !it.isJsonNull }
            ?.asString
            ?.takeIf { it.isNotBlank() }
    }
}

private data class ItemSize(
    val name: String,
    val bytes: Int,
    val fields: List<String>
)