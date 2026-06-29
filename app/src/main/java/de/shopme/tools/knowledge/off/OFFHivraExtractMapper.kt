package de.shopme.tools.knowledge.off

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.shopme.tools.knowledge.dimension.KnowledgeDimensionId


class OFFHivraExtractMapper {

    fun map(
        product: JsonObject
    ): OFFHivraExtract? {

        if (!isFood(product)) {
            return null
        }

        val productName =
            firstNonBlank(
                product.stringOrNull("product_name"),
                product.stringOrNull("product_name_de"),
                product.stringOrNull("product_name_en"),
                product.stringOrNull("product_name_fr")
            ) ?: return null

        if (!isRelevantLanguageOrCountry(product)) {
            return null
        }

        val carbon =
            extractCarbon(product)

        val availableDimensions =
            findAvailableDimensions(
                product = product,
                carbon = carbon
            )

        val missingDimensions =
            KnowledgeDimensionId.entries
                .filter {
                    it in extractRelevantDimensions
                }
                .filterNot {
                    it in availableDimensions
                }
                .toSet()

        return OFFHivraExtract(

            code =
                firstNonBlank(
                    product.stringOrNull("code"),
                    product.stringOrNull("_id"),
                    product.stringOrNull("id")
                ),

            productName = productName,

            productNameDe =
                product.stringOrNull("product_name_de"),

            productNameEn =
                product.stringOrNull("product_name_en"),

            productNameFr =
                product.stringOrNull("product_name_fr"),

            brands =
                product.stringOrNull("brands"),

            countriesTags =
                product.stringList("countries_tags"),

            languagesTags =
                product.stringList("languages_tags"),

            categoriesTags =
                product.stringList("categories_tags"),

            categoriesHierarchy =
                product.stringList("categories_hierarchy"),

            ingredientsText =
                firstNonBlank(
                    product.stringOrNull("ingredients_text"),
                    product.stringOrNull("ingredients_text_de"),
                    product.stringOrNull("ingredients_text_en"),
                    product.stringOrNull("ingredients_text_fr")
                ),

            ingredientsTags =
                product.stringList("ingredients_tags"),

            allergensTags =
                product.stringList("allergens_tags"),

            tracesTags =
                product.stringList("traces_tags"),

            nutriments =
                product
                    .objectOrNull("nutriments")
                    ?.toDoubleMap()
                    ?: emptyMap(),

            nutriscoreGrade =
                firstNonBlank(
                    product.stringOrNull("nutriscore_grade"),
                    product.stringOrNull("nutrition_grades")
                ),

            novaGroup =
                product.intOrNull("nova_group")
                    ?: product.intOrNull("nova_groups"),

            packagingMaterialsTags =
                product.stringList("packaging_materials_tags"),

            packagingShapesTags =
                product.stringList("packaging_shapes_tags"),

            originsTags =
                product.stringList("origins_tags"),

            manufacturingPlaces =
                product.stringOrNull("manufacturing_places"),

            labelsTags =
                product.stringList("labels_tags"),

            ecoscoreGrade =
                product.stringOrNull("ecoscore_grade"),

            environmentalScoreGrade =
                product.stringOrNull("environmental_score_grade"),

            carbon =
                carbon,

            availableDimensions =
                availableDimensions,

            missingDimensions =
                missingDimensions
        )
    }

    private fun isFood(
        product: JsonObject
    ): Boolean {

        val productType =
            product.stringOrNull("product_type")

        return productType == null ||
                productType == "food"
    }

    private fun isRelevantLanguageOrCountry(
        product: JsonObject
    ): Boolean {

        val languages =
            product.stringList("languages_tags")

        val countries =
            product.stringList("countries_tags")

        return languages.any {
            it in relevantLanguageTags
        } || countries.any {
            it in relevantCountryTags
        }
    }

    private fun JsonObject.doubleOrNull(
        key: String
    ): Double? {

        val value =
            get(key)
                ?: return null

        if (value.isJsonNull) {
            return null
        }

        return runCatching {
            value.asDouble
        }.getOrNull()
    }

    private fun findAvailableDimensions(
        product: JsonObject,
        carbon: OFFCarbonExtract?
    ): Set<KnowledgeDimensionId> {

        val result =
            mutableSetOf<KnowledgeDimensionId>()

        if (hasNutriScore(product)) {
            result += KnowledgeDimensionId.NUTRI_SCORE
        }

        if (
            carbon
                ?.co2Total
                ?.let {
                    it > 0.0
                } == true
        ) {
            result += KnowledgeDimensionId.CARBON
        }

        if (hasNutrition(product)) {
            result += KnowledgeDimensionId.NUTRITION
        }

        if (
            product.stringList("allergens_tags").isNotEmpty() ||
            product.stringList("allergens_hierarchy").isNotEmpty()
        ) {
            result += KnowledgeDimensionId.ALLERGENS
        }

        if (
            product.stringOrNull("ingredients_text").isNullOrBlank().not() ||
            product.stringList("ingredients_tags").isNotEmpty()
        ) {
            result += KnowledgeDimensionId.INGREDIENTS
        }

        if (
            product.stringList("categories_tags").isNotEmpty() ||
            product.stringList("categories_hierarchy").isNotEmpty()
        ) {
            result += KnowledgeDimensionId.FOOD_TAXONOMY
        }

        if (
            product.intOrNull("nova_group") != null ||
            product.intOrNull("nova_groups") != null
        ) {
            result += KnowledgeDimensionId.PROCESSING
        }

        if (
            product.stringList("packaging_materials_tags").isNotEmpty() ||
            product.stringList("packaging_shapes_tags").isNotEmpty()
        ) {
            result += KnowledgeDimensionId.PACKAGING
        }

        if (
            product.stringList("origins_tags").isNotEmpty() ||
            product.stringOrNull("manufacturing_places").isNullOrBlank().not()
        ) {
            result += KnowledgeDimensionId.LOCALITY
            result += KnowledgeDimensionId.FOOD_MILES
        }

        if (product.stringList("labels_tags").isNotEmpty()) {
            result += KnowledgeDimensionId.PRODUCTION
        }

        return result
    }

    private fun hasNutrition(
        product: JsonObject
    ): Boolean {

        val nutriments =
            product.objectOrNull("nutriments")
                ?: return false

        return nutriments.has("energy-kcal_100g") ||
                nutriments.has("energy_100g") ||
                nutriments.has("fat_100g") ||
                nutriments.has("saturated-fat_100g") ||
                nutriments.has("carbohydrates_100g") ||
                nutriments.has("sugars_100g") ||
                nutriments.has("fiber_100g") ||
                nutriments.has("proteins_100g") ||
                nutriments.has("salt_100g")
    }

    private fun JsonObject.stringOrNull(
        key: String
    ): String? {

        val value =
            get(key)
                ?: return null

        if (value.isJsonNull) {
            return null
        }

        return runCatching {
            value.asString
        }.getOrNull()
            ?.takeIf {
                it.isNotBlank()
            }
    }

    private fun JsonObject.intOrNull(
        key: String
    ): Int? {

        val value =
            get(key)
                ?: return null

        if (value.isJsonNull) {
            return null
        }

        return runCatching {
            value.asInt
        }.getOrNull()
    }

    private fun JsonObject.objectOrNull(
        key: String
    ): JsonObject? {

        val value =
            get(key)
                ?: return null

        if (value.isJsonNull || !value.isJsonObject) {
            return null
        }

        return value.asJsonObject
    }

    private fun JsonObject.stringList(
        key: String
    ): List<String> {

        val value =
            get(key)
                ?: return emptyList()

        if (value.isJsonNull || !value.isJsonArray) {
            return emptyList()
        }

        return value
            .asJsonArray
            .mapNotNull {
                runCatching {
                    it.asString
                }.getOrNull()
            }
            .filter {
                it.isNotBlank()
            }
    }

    private fun JsonObject.toDoubleMap(): Map<String, Double?> {

        return entrySet()
            .associate { entry ->

                entry.key to entry.value.doubleOrNull()
            }
    }

    private fun JsonElement.doubleOrNull(): Double? {

        if (isJsonNull) {
            return null
        }

        return runCatching {
            asDouble
        }.getOrNull()
    }

    private fun firstNonBlank(
        vararg values: String?
    ): String? {

        return values
            .firstOrNull {
                !it.isNullOrBlank()
            }
    }

    private companion object {

        val validNutriScoreGrades =
            setOf(
                "a",
                "b",
                "c",
                "d",
                "e"
            )

        val relevantLanguageTags =
            setOf(
                "en:german",
                "en:english",
                "en:french"
            )

        val relevantCountryTags =
            setOf(
                "en:germany",
                "en:deutschland",
                "en:france",
                "en:united-kingdom",
                "en:united-states"
            )

        val extractRelevantDimensions =
            setOf(
                KnowledgeDimensionId.NUTRITION,
                KnowledgeDimensionId.ALLERGENS,
                KnowledgeDimensionId.INGREDIENTS,
                KnowledgeDimensionId.FOOD_TAXONOMY,
                KnowledgeDimensionId.NUTRI_SCORE,
                KnowledgeDimensionId.PROCESSING,
                KnowledgeDimensionId.PACKAGING,
                KnowledgeDimensionId.LOCALITY,
                KnowledgeDimensionId.FOOD_MILES,
                KnowledgeDimensionId.CARBON,
                KnowledgeDimensionId.PRODUCTION,
                KnowledgeDimensionId.FAIR_TRADE,
                KnowledgeDimensionId.ANIMAL_WELFARE
            )
    }

    private fun extractCarbon(
        product: JsonObject
    ): OFFCarbonExtract? {

        val agribalyse =

            product
                .objectOrNull("environmental_score_data")
                ?.objectOrNull("agribalyse")

                ?: product
                    .objectOrNull("ecoscore_data")
                    ?.objectOrNull("agribalyse")

                ?: return null

        val carbon =

            OFFCarbonExtract(

                co2Total =
                    agribalyse.doubleOrNull("co2_total"),

                co2Agriculture =
                    agribalyse.doubleOrNull("co2_agriculture"),

                co2Processing =
                    agribalyse.doubleOrNull("co2_processing"),

                co2Packaging =
                    agribalyse.doubleOrNull("co2_packaging"),

                co2Transportation =
                    agribalyse.doubleOrNull("co2_transportation")
            )

        return if (

            carbon.co2Total == null &&
            carbon.co2Agriculture == null &&
            carbon.co2Processing == null &&
            carbon.co2Packaging == null &&
            carbon.co2Transportation == null

        ) {
            null
        } else {
            carbon
        }
    }

    private fun hasNutriScore(
        product: JsonObject
    ): Boolean {

        val grade =
            firstNonBlank(
                product.stringOrNull("nutriscore_grade"),
                product.stringOrNull("nutrition_grades")
            )
                ?.lowercase()

        return grade in validNutriScoreGrades
    }
}