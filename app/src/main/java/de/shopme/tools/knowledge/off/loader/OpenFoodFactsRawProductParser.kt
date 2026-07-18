package de.shopme.tools.knowledge.off.loader

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.ai.sources.off.OFFRawProduct

class OpenFoodFactsRawProductParser {

    fun parseLine(
        line: String
    ): OFFRawProduct? {
        val json =
            runCatching {
                JsonParser.parseString(line).asJsonObject
            }.getOrNull()
                ?: return null

        val code =
            json.string("_id")
                ?: json.string("code")
                ?: return null

        return OFFRawProduct(
            code = code,
            productName = json.string("product_name"),
            genericName = json.string("generic_name"),
            brands = json.string("brands"),
            categories = json.string("categories"),
            ingredientsText = json.string("ingredients_text"),
            labels = json.string("labels"),
            countries = json.string("countries"),
            origins = json.string("origins"),
            allergens = json.string("allergens"),
            packaging = json.string("packaging"),
            manufacturingPlaces = json.string("manufacturing_places"),
            nutritionGradeFr = json.string("nutrition_grade_fr"),
            novaGroup = json.int("nova_group"),
            energyKcal100g = json.doubleFromNutriments("energy-kcal_100g"),
            fat100g = json.doubleFromNutriments("fat_100g"),
            saturatedFat100g = json.doubleFromNutriments("saturated-fat_100g"),
            carbohydrates100g = json.doubleFromNutriments("carbohydrates_100g"),
            sugars100g = json.doubleFromNutriments("sugars_100g"),
            fiber100g = json.doubleFromNutriments("fiber_100g"),
            proteins100g = json.doubleFromNutriments("proteins_100g"),
            salt100g = json.doubleFromNutriments("salt_100g")
        )
    }

    fun parseLines(
        lines: List<String>
    ): List<OFFRawProduct> {
        return lines.mapNotNull(::parseLine)
    }

    private fun JsonObject.string(
        key: String
    ): String? {
        return get(key)
            ?.takeIf { !it.isJsonNull }
            ?.asString
            ?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.int(
        key: String
    ): Int? {
        return runCatching {
            get(key)
                ?.takeIf { !it.isJsonNull }
                ?.asInt
        }.getOrNull()
    }

    private fun JsonObject.doubleFromNutriments(
        key: String
    ): Double? {
        val nutriments =
            getAsJsonObject("nutriments")
                ?: return null

        return runCatching {
            nutriments.get(key)
                ?.takeIf { !it.isJsonNull }
                ?.asDouble
        }.getOrNull()
    }
}