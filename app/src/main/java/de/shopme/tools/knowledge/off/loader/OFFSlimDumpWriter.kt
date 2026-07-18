package de.shopme.tools.knowledge.off.loader

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class OFFSlimDumpWriter {

    fun writeSlimDump(
        inputFile: File,
        outputFile: File,
        maxRecords: Int? = null
    ): SlimDumpResult {
        require(inputFile.exists()) {
            "OFF dump not found: ${inputFile.absolutePath}"
        }

        require(maxRecords == null || maxRecords > 0) {
            "maxRecords must be null or greater than zero."
        }

        outputFile.parentFile?.mkdirs()

        var read = 0
        var written = 0
        var skipped = 0

        GZIPInputStream(inputFile.inputStream())
            .bufferedReader()
            .use { reader ->

                GZIPOutputStream(outputFile.outputStream())
                    .use { gzip ->

                        BufferedWriter(OutputStreamWriter(gzip))
                            .use { writer ->

                                while (maxRecords == null || read < maxRecords) {
                                    val line =
                                        reader.readLine()
                                            ?: break

                                    read++

                                    val slim =
                                        toSlimJson(line)

                                    if (slim == null) {
                                        skipped++
                                        continue
                                    }

                                    writer.write(slim.toString())
                                    writer.newLine()
                                    written++

                                    if (written % 100_000 == 0) {
                                        println("OFF slim dump written=$written read=$read skipped=$skipped")
                                    }
                                }
                            }
                    }
            }

        return SlimDumpResult(
            read = read,
            written = written,
            skipped = skipped,
            outputFile = outputFile
        )
    }

    private fun toSlimJson(
        line: String
    ): JsonObject? {
        val source =
            runCatching {
                JsonParser.parseString(line).asJsonObject
            }.getOrNull()
                ?: return null

        val code =
            source.string("_id")
                ?: source.string("code")
                ?: return null

        val target = JsonObject()

        target.addProperty("code", code)

        copyString(source, target, "product_name")
        copyString(source, target, "product_name_en")
        copyString(source, target, "product_name_de")

        copyString(source, target, "generic_name")
        copyString(source, target, "generic_name_en")
        copyString(source, target, "generic_name_de")

        copyString(source, target, "lang")
        copyString(source, target, "lc")
        copyObject(source, target, "languages")
        copyObject(source, target, "languages_codes")
        copyArray(source, target, "languages_tags")

        copyString(source, target, "brands")
        copyString(source, target, "categories")

        copyString(source, target, "ingredients_text")
        copyString(source, target, "ingredients_text_en")
        copyString(source, target, "ingredients_text_de")

        copyString(source, target, "labels")
        copyString(source, target, "countries")
        copyString(source, target, "origins")
        copyString(source, target, "allergens")
        copyString(source, target, "packaging")

        copyString(source, target, "packaging_text")
        copyArray(source, target, "packaging_tags")
        copyArray(source, target, "packaging_materials_tags")
        copyArray(source, target, "packaging_shapes_tags")

        copyArray(source, target, "categories_tags")
        copyArray(source, target, "categories_hierarchy")
        copyString(source, target, "main_category")

        copyString(source, target, "manufacturing_places")
        copyString(source, target, "nutrition_grade_fr")
        copyNumber(source, target, "nova_group")

        copyArray(source, target, "labels_tags")
        copyArray(source, target, "categories_tags")

        copyArray(source, target, "allergens_tags")
        copyArray(source, target, "traces_tags")

        copyArray(source, target, "countries_tags")
        copyArray(source, target, "origins_tags")
        copyArray(source, target, "manufacturing_places_tags")


        val nutriments =
            source.getAsJsonObject("nutriments")

        if (nutriments != null) {
            val slimNutriments = JsonObject()

            copyNumber(nutriments, slimNutriments, "energy-kcal_100g")
            copyNumber(nutriments, slimNutriments, "fat_100g")
            copyNumber(nutriments, slimNutriments, "saturated-fat_100g")
            copyNumber(nutriments, slimNutriments, "carbohydrates_100g")
            copyNumber(nutriments, slimNutriments, "sugars_100g")
            copyNumber(nutriments, slimNutriments, "fiber_100g")
            copyNumber(nutriments, slimNutriments, "proteins_100g")
            copyNumber(nutriments, slimNutriments, "salt_100g")

            if (slimNutriments.size() > 0) {
                target.add("nutriments", slimNutriments)
            }
        }

        return target
    }

    private fun copyObject(
        source: JsonObject,
        target: JsonObject,
        key: String
    ) {
        val value =
            source.get(key)
                ?.takeIf { !it.isJsonNull && it.isJsonObject }
                ?: return

        target.add(key, value)
    }

    private fun copyArray(
        source: JsonObject,
        target: JsonObject,
        key: String
    ) {
        val value =
            source.get(key)
                ?.takeIf { !it.isJsonNull && it.isJsonArray }
                ?: return

        target.add(key, value)
    }

    private fun copyString(
        source: JsonObject,
        target: JsonObject,
        key: String
    ) {
        val value =
            source.string(key)
                ?: return

        target.addProperty(key, value)
    }

    private fun copyNumber(
        source: JsonObject,
        target: JsonObject,
        key: String
    ) {
        val value =
            runCatching {
                source.get(key)
                    ?.takeIf { !it.isJsonNull }
                    ?.asDouble
            }.getOrNull()
                ?: return

        target.addProperty(key, value)
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

data class SlimDumpResult(
    val read: Int,
    val written: Int,
    val skipped: Int,
    val outputFile: File
)