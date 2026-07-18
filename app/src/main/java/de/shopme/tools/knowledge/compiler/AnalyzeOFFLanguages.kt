package de.shopme.tools.knowledge.compiler

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.zip.GZIPInputStream

object AnalyzeOFFLanguages {

    @JvmStatic
    fun main(args: Array<String>) {

        val input =
            File("../data/generated/openfoodfacts/openfoodfacts-products.slim.jsonl.gz")

        require(input.exists()) {
            "OFF slim dump not found: ${input.absolutePath}"
        }

        var scanned = 0L

        var english = 0L
        var german = 0L
        var both = 0L
        var withoutEnglishOrGerman = 0L

        val englishNames = linkedSetOf<String>()
        val germanNames = linkedSetOf<String>()

        GZIPInputStream(input.inputStream())
            .bufferedReader()
            .useLines { lines ->

                lines.forEach { line ->

                    scanned++

                    val json =
                        JsonParser
                            .parseString(line)
                            .asJsonObject

                    val englishName =
                        json.string("product_name_en")

                    val germanName =
                        json.string("product_name_de")

                    val hasEnglish =
                        englishName != null

                    val hasGerman =
                        germanName != null

                    if (englishName != null) {
                        english++
                        englishNames.add(englishName)
                    }

                    if (germanName != null) {
                        german++
                        germanNames.add(germanName)
                    }

                    if (hasEnglish && hasGerman) {
                        both++
                    }

                    if (!hasEnglish && !hasGerman) {
                        withoutEnglishOrGerman++
                    }

                    if (scanned % 100_000 == 0L) {
                        println(
                            "Scanned=$scanned en=$english de=$german both=$both missing=$withoutEnglishOrGerman"
                        )
                    }
                }
            }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("OFF LOCALIZED NAME STATISTICS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Products scanned       : $scanned")
        println()
        println("Products with EN name  : $english")
        println("Products with DE name  : $german")
        println("Products with both     : $both")
        println("Products without EN/DE : $withoutEnglishOrGerman")
        println()
        println("Unique EN names        : ${englishNames.size}")
        println("Unique DE names        : ${germanNames.size}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun JsonObject.string(
        key: String
    ): String? {
        val value =
            get(key)
                ?: return null

        if (
            value.isJsonNull ||
            !value.isJsonPrimitive
        ) {
            return null
        }

        return value
            .asString
            .trim()
            .lowercase()
            .takeIf { it.isNotBlank() }
    }
}