package de.shopme.tools.report

import android.content.Context
import com.google.gson.JsonParser

class RuntimeKnowledgeCoverageCalculator(
    private val readAssetText: (String) -> String
) {

    constructor(
        context: Context
    ) : this(
        readAssetText = { assetPath ->
            context.assets
                .open(assetPath)
                .bufferedReader()
                .use {
                    it.readText()
                }
        }
    )


    fun calculate(
        total: Int
    ): List<FoodKnowledgeCoverageEntry> =
        listOf(
            entry(
                CoverageDimension.ALLERGEN,
                "allergens.json",
                total
            ),
            entry(
                CoverageDimension.ANIMALWELFARE,
                "animal_welfare.json",
                total
            ),
            entry(
                CoverageDimension.BIODIVERSITY,
                "biodiversity.json",
                total
            ),
            entry(
                CoverageDimension.FAIRTRADE,
                "fairtrade.json",
                total
            ),
            entry(
                CoverageDimension.FOODMILES,
                "food_miles.json",
                total
            ),
            entry(
                CoverageDimension.INGREDIENT,
                "ingredients.json",
                total
            ),
            entry(
                CoverageDimension.LOCALITY,
                "locality.json",
                total
            ),
            entry(
                CoverageDimension.NUTRITION,
                "nutrition.json",
                total
            ),
            entry(
                CoverageDimension.NUTRISCORE,
                "nutri_score.json",
                total
            ),
            entry(
                CoverageDimension.PACKAGING,
                "packaging.json",
                total
            ),
            entry(
                CoverageDimension.PESTICIDE,
                "pesticides.json",
                total
            ),
            entry(
                CoverageDimension.POLLINATOR,
                "pollinator.json",
                total
            ),
            entry(
                CoverageDimension.PROCESSING,
                "processing.json",
                total
            ),
            entry(
                CoverageDimension.PRODUCTION,
                "production.json",
                total
            ),
            entry(
                CoverageDimension.RECIPE,
                "recipes.json",
                total
            ),
            entry(
                CoverageDimension.TAXONOMY,
                "food_taxonomy.json",
                total
            ),
            entry(
                CoverageDimension.WATER,
                "water_footprint.json",
                total
            ),
            entry(
                CoverageDimension.WATERSTRESS,
                "water_stress.json",
                total
            ),
            entry(
                CoverageDimension.DIET,
                "diet_classification.json",
                total
            ),
            entry(
                CoverageDimension.SEASONALITY,
                "seasonality.json",
                total
            )
        )


    private fun entry(
        dimension: CoverageDimension,
        file: String,
        total: Int
    ): FoodKnowledgeCoverageEntry =
        FoodKnowledgeCoverageEntry(
            name = dimension.displayName,
            covered =
                countEntries(
                    file = file
                ),
            total = total
        )


    private fun countEntries(
        file: String
    ): Int {

        val json =
            readAssetText(
                "knowledge/runtime/$file"
            )

        val root =
            JsonParser
                .parseString(json)
                .asJsonObject

        val entries =
            root["entries"]

        require(
            entries != null &&
                    entries.isJsonObject
        ) {
            "Runtime knowledge asset has no entries object: $file"
        }

        return entries
            .asJsonObject
            .size()
    }
}