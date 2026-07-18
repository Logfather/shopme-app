package de.shopme.tools.knowledge.compiler

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.util.zip.GZIPInputStream

object AnalyzeOFFNormalizedNames {

    @JvmStatic
    fun main(args: Array<String>) {
        val inputFile =
            if (args.isNotEmpty()) File(args[0])
            else File("../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz")

        require(inputFile.exists()) {
            "OFF slim dump not found: ${inputFile.absolutePath}"
        }

        val gson = Gson()

        val countsByNormalizedName =
            linkedMapOf<String, Int>()

        var total = 0L
        var missingNormalized = 0L

        GZIPInputStream(inputFile.inputStream())
            .bufferedReader()
            .useLines { lines ->
                lines.forEach { line ->
                    if (line.isBlank()) {
                        return@forEach
                    }

                    total++

                    val item =
                        gson.fromJson(line, JsonObject::class.java)

                    val normalized =
                        item.string("normalized")
                            ?: item.string("normalizedName")
                            ?: item.string("normalized_name")
                            ?: item.string("product_name")
                            ?: item.string("product_name_en")

                    if (normalized == null) {
                        missingNormalized++
                    } else {
                        countsByNormalizedName[normalized] =
                            countsByNormalizedName.getOrDefault(normalized, 0) + 1
                    }

                    if (total % 100_000 == 0L) {
                        println("Scanned=$total unique=${countsByNormalizedName.size}")
                    }
                }
            }

        val unique =
            countsByNormalizedName.size

        val duplicateProducts =
            countsByNormalizedName.values.sumOf { count ->
                (count - 1).coerceAtLeast(0)
            }

        val duplicateNames =
            countsByNormalizedName.values.count { count ->
                count > 1
            }

        val topDuplicates =
            countsByNormalizedName.entries
                .filter { it.value > 1 }
                .sortedByDescending { it.value }
                .take(50)

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF NORMALIZED NAME STATISTICS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Input              : ${inputFile.absolutePath}")
        println("Products scanned   : $total")
        println("Unique names       : $unique")
        println("Missing names      : $missingNormalized")
        println("Duplicate products : $duplicateProducts")
        println("Duplicate names    : $duplicateNames")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Top duplicate normalized names:")
        topDuplicates.forEach { (name, count) ->
            println("  $count × $name")
        }
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun JsonObject.string(
        key: String
    ): String? {
        val value =
            get(key)
                ?: return null

        if (value.isJsonNull || !value.isJsonPrimitive) {
            return null
        }

        return value.asString
            .trim()
            .lowercase()
            .takeIf { it.isNotBlank() }
    }
}