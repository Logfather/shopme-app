package de.shopme.tools.knowledge.foods.importer

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.shopme.tools.knowledge.foods.FoodKnowledgeSource
import de.shopme.tools.knowledge.foods.FoodKnowledgeSourceEntry
import de.shopme.tools.knowledge.foods.FoodKnowledgeSources
import de.shopme.tools.knowledge.foods.FoodNames
import de.shopme.tools.knowledge.foods.FoodsKnowledge
import de.shopme.tools.knowledge.importer.OFFFoodsKnowledgeImportResult
import de.shopme.tools.knowledge.nutrition.NutritionFacts
import java.io.File
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

class OFFFoodsKnowledgeImporter {

    fun import(
        input: File,
        limit: Int? = null,
        progressStep: Int = 100_000
    ): FoodsKnowledge {

        return importWithStatistics(
            input = input,
            limit = limit,
            progressStep = progressStep
        ).knowledge
    }

    fun importWithStatistics(
        input: File,
        limit: Int? = null,
        progressStep: Int = 100_000
    ): OFFFoodsKnowledgeImportResult {

        require(input.exists()) {
            "Open Food Facts dump not found: ${input.absolutePath}"
        }

        val foods =
            mutableMapOf<String, FoodKnowledgeSourceEntry>()

        val nameCounts =
            mutableMapOf<String, Int>()

        var scanned = 0
        var imported = 0

        GZIPInputStream(
            input.inputStream()
        ).use { gzip ->

            InputStreamReader(gzip)
                .buffered()
                .useLines { lines ->

                    for (line in lines) {

                        if (limit != null && scanned >= limit) {
                            break
                        }

                        scanned++

                        val entry =
                            parseEntry(
                                line = line
                            )
                                ?: continue

                        foods[entry.id] =
                            entry

                        nameCounts[entry.id] =
                            nameCounts.getOrDefault(
                                entry.id,
                                0
                            ) + 1

                        imported++

                        if (progressStep > 0 && scanned % progressStep == 0) {
                            println(
                                "OFF import scanned=$scanned imported=$imported unique=${foods.size}"
                            )
                        }
                    }
                }
        }

        println(
            "OFF import finished scanned=$scanned imported=$imported unique=${foods.size}"
        )

        val knowledge =
            FoodsKnowledge(
                version = 1,
                foods =
                    foods.values
                        .sortedBy {
                            it.id
                        }
            )

        return OFFFoodsKnowledgeImportResult(
            knowledge = knowledge,
            scanned = scanned,
            imported = imported,
            unique = foods.size,
            nameCounts =
                nameCounts
                    .toSortedMap()
        )
    }

    private fun isValidFoodName(
        name: String
    ): Boolean {

        val cleaned =
            name.trim()

        if (cleaned.isBlank()) return false
        if (cleaned.length < 2) return false
        if (cleaned.all { it.isDigit() }) return false
        if (cleaned.count { it.isLetter() } < 2) return false

        return true
    }

    private fun parseEntry(
        line: String
    ): FoodKnowledgeSourceEntry? {

        val json =
            runCatching {
                JsonParser
                    .parseString(line)
                    .asJsonObject
            }.getOrNull()
                ?: return null

        val name =
            json.get("product_name")
                ?.asString
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: return null

        val nutriments =
            json.getAsJsonObject("nutriments")
                ?: return null

        val normalizedName =
            normalize(
                value = name
            )

        if (!isValidFoodName(normalizedName)) {
            return null
        }

        val nutritionFacts =
            NutritionFacts(
                calories =
                    nutriments.doubleOrZero(
                        "energy-kcal_100g"
                    ),
                protein =
                    nutriments.doubleOrZero(
                        "proteins_100g"
                    ),
                fat =
                    nutriments.doubleOrZero(
                        "fat_100g"
                    ),
                saturatedFat =
                    nutriments.doubleOrZero(
                        "saturated-fat_100g"
                    ),
                carbohydrates =
                    nutriments.doubleOrZero(
                        "carbohydrates_100g"
                    ),
                sugar =
                    nutriments.doubleOrZero(
                        "sugars_100g"
                    ),
                fiber =
                    nutriments.doubleOrZero(
                        "fiber_100g"
                    ),
                salt =
                    nutriments.doubleOrZero(
                        "salt_100g"
                    )
            )

        return FoodKnowledgeSourceEntry(
            id = normalizedName,
            names =
                FoodNames(
                    canonical = normalizedName,
                    aliases =
                        listOf(
                            name
                        )
                ),
            knowledge =
                FoodKnowledgeSources(
                    nutrition =
                        FoodKnowledgeSource(
                            reference = normalizedName,
                            source = "open_food_facts",
                            value = nutritionFacts
                        )
                )
        )
    }

    private fun normalize(
        value: String
    ): String {

        return value
            .lowercase()
            .replace("ä", "ae")
            .replace("ö", "oe")
            .replace("ü", "ue")
            .replace("ß", "ss")
            .replace(Regex("[^a-z0-9]+"), " ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }

    private fun JsonObject.doubleOrZero(
        key: String
    ): Double {

        return get(key)
            ?.takeIf {
                !it.isJsonNull
            }
            ?.runCatching {
                asDouble
            }
            ?.getOrNull()
            ?: 0.0
    }
}