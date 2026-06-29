package de.shopme.testing.system.tools.knowledge.off

import com.google.gson.JsonParser
import org.junit.Test
import java.io.File

class OFFHivraExtractAnalysisTest {

    @Test
    fun analyzeHivraExtract() {

        val input =
            File(
                "build/off/off_hivra_extract.jsonl"
            )

        require(input.exists()) {
            "Hivra OFF extract not found: ${input.absolutePath}"
        }

        var total =
            0

        val availableDimensionCounts =
            mutableMapOf<String, Int>()

        val missingDimensionCounts =
            mutableMapOf<String, Int>()

        val countryCounts =
            mutableMapOf<String, Int>()

        val languageCounts =
            mutableMapOf<String, Int>()

        var withNutrition =
            0

        var withAllergens =
            0

        var withIngredients =
            0

        var withPackaging =
            0

        var withLocality =
            0

        var withCarbon =
            0

        input
            .bufferedReader()
            .useLines { lines ->

                lines.forEach { line ->

                    if (line.isBlank()) {
                        return@forEach
                    }

                    val json =
                        runCatching {
                            JsonParser
                                .parseString(line)
                                .asJsonObject
                        }.getOrNull()
                            ?: return@forEach

                    total++

                    val available =
                        json.stringArray("availableDimensions")

                    val missing =
                        json.stringArray("missingDimensions")

                    available.forEach { dimension ->

                        availableDimensionCounts[dimension] =
                            (availableDimensionCounts[dimension] ?: 0) + 1
                    }

                    missing.forEach { dimension ->

                        missingDimensionCounts[dimension] =
                            (missingDimensionCounts[dimension] ?: 0) + 1
                    }

                    json
                        .stringArray("countriesTags")
                        .forEach { country ->

                            countryCounts[country] =
                                (countryCounts[country] ?: 0) + 1
                        }

                    json
                        .stringArray("languagesTags")
                        .forEach { language ->

                            languageCounts[language] =
                                (languageCounts[language] ?: 0) + 1
                        }

                    if ("NUTRITION" in available) {
                        withNutrition++
                    }

                    if ("ALLERGENS" in available) {
                        withAllergens++
                    }

                    if ("INGREDIENTS" in available) {
                        withIngredients++
                    }

                    if ("PACKAGING" in available) {
                        withPackaging++
                    }

                    if ("LOCALITY" in available) {
                        withLocality++
                    }

                    if ("CARBON" in available) {
                        withCarbon++
                    }

                    if (total % 100_000 == 0) {
                        println(
                            "Analyzed extract entries=$total"
                        )
                    }
                }
            }

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 OPEN FOOD FACTS HIVRA EXTRACT ANALYSIS")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Entries      : $total")
        println()
        println("Core Dimensions:")
        println("Nutrition    : $withNutrition")
        println("Allergens    : $withAllergens")
        println("Ingredients  : $withIngredients")
        println("Packaging    : $withPackaging")
        println("Locality     : $withLocality")
        println("Carbon       : $withCarbon")

        println()
        println("Available Dimensions:")
        printTopMap(
            availableDimensionCounts
        )

        println()
        println("Missing Dimensions:")
        printTopMap(
            missingDimensionCounts
        )

        println()
        println("Top Countries:")
        printTopMap(
            countryCounts,
            limit = 20
        )

        println()
        println("Top Languages:")
        printTopMap(
            languageCounts,
            limit = 20
        )

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    }

    private fun com.google.gson.JsonObject.stringArray(
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
            .mapNotNull { element ->

                runCatching {
                    element.asString
                }.getOrNull()
            }
            .filter {
                it.isNotBlank()
            }
    }

    private fun printTopMap(

        values: Map<String, Int>,

        limit: Int = 50

    ) {

        if (values.isEmpty()) {
            println("- none")
            return
        }

        values
            .entries
            .sortedByDescending {
                it.value
            }
            .take(limit)
            .forEach { entry ->

                println(
                    "- ${entry.key}: ${entry.value}"
                )
            }
    }
}