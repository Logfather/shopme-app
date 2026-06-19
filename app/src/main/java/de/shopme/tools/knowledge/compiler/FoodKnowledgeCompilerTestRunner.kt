package de.shopme.tools.knowledge.compiler

import de.shopme.domain.catalog.CatalogItem
import de.shopme.domain.food.FoodKnowledgeEntry
import de.shopme.tools.knowledge.biodiversity.BiodiversityClassifier
import de.shopme.tools.knowledge.carbon.CarbonImpactClassifier
import de.shopme.tools.knowledge.foodmiles.FoodMilesClassifier
import de.shopme.tools.knowledge.packaging.PackagingImpactClassifier
import de.shopme.tools.knowledge.pollinator.PollinatorClassifier
import de.shopme.tools.knowledge.waterfootprint.WaterImpactClassifier

class FoodKnowledgeCompilerTestRunner(

    private val compiler: FoodKnowledgeCompiler

) {

    fun run(
        items: List<CatalogItem>
    ): String {

        return buildString {

            appendLine("========================================")
            appendLine("        HIVRA KNOWLEDGE ENGINE")
            appendLine("========================================")
            appendLine()

            items.forEach {

                append(

                    formatEntry(

                        compiler.compile(it)

                    )

                )

            }

        }

    }

    private fun formatEntry(
        entry: FoodKnowledgeEntry
    ): String = buildString {

        appendLine("----------------------------------------")

        appendLine("Input")
        appendLine()
        appendLine(entry.inputName)
        appendLine()

        appendLine()

        appendLine("----------------------------------------")

        appendLine()

        appendLine("Normalized")

        appendLine()

        appendLine(entry.normalizedName)

        appendLine()

        appendLine("----------------------------------------")

        appendLine()

        appendLine("Nutrition Reference")

        appendLine()

        appendLine(entry.nutritionReference ?: "-")

        appendLine("----------------------------------------")
        appendLine()
        appendLine("Taxonomy Path")
        appendLine()

        if (entry.taxonomyPath.isEmpty()) {

            appendLine("-")

        } else {

            entry.taxonomyPath.forEach {

                appendLine(it)

            }

        }

        appendLine("----------------------------------------")
        appendLine()

        appendLine("Category")
        appendLine()
        appendLine(entry.category)
        appendLine()

        appendLine("----------------------------------------")
        appendLine()

        appendLine("Diet")
        appendLine()

        if (entry.dietClassifications.isEmpty()) {

            appendLine("-")

        } else {

            entry.dietClassifications
                .sortedBy { it.name }
                .forEach {

                    appendLine("✓ $it")

                }

        }

        appendLine()

        appendLine("----------------------------------------")
        appendLine()

        appendLine("Glycemic Index")
        appendLine()
        appendLine(entry.glycemicIndex)
        appendLine()

        appendLine("----------------------------------------")
        appendLine()

        appendLine("Allergens")
        appendLine()

        if (entry.allergens.isEmpty()) {

            appendLine("-")

        } else {

            entry.allergens
                .sortedBy { it.name }
                .forEach {

                    appendLine("• $it")

                }

        }

        appendLine()

        appendLine("--------------------------------")

        appendLine("Production")

        appendLine()

        if (
            entry.production.isEmpty()
        ) {

            appendLine("-")

        } else {

            entry.production.forEach {

                appendLine("• $it")

            }

        }

        appendLine("--------------------------------")

        appendLine("Production")

        appendLine()

        if (entry.production.isEmpty()) {

            appendLine("-")

        } else {

            entry.production.forEach { method ->

                entry.production.forEach {

                    appendLine("• ${it.name}")

                }

            }

        }

        appendLine("----------------------------------------")
        appendLine()

        appendLine("Tags")
        appendLine()

        if (entry.tags.isEmpty()) {

            appendLine("-")

        } else {

            entry.tags
                .sortedBy { it.name }
                .forEach {

                    appendLine("• $it")

                }

        }

        appendLine()

        appendLine("----------------------------------------")
        appendLine()

        appendLine("Seasonality")
        appendLine()

        if (entry.seasonality.isEmpty()) {

            appendLine("-")

        } else {

            appendLine(

                entry.seasonality.joinToString(", ")

            )

        }

        appendLine()

        appendLine("--------------------------------")

        appendLine("Carbon Footprint")

        appendLine()

        entry.carbonFootprint?.let { footprint ->

            val impactLevel =

                CarbonImpactClassifier()

                    .classify(
                        footprint
                    )

            appendLine(impactLevel.name)

            appendLine()

            appendLine(
                "${footprint.kilogramsPerKilogram} kg CO₂/kg"
            )

        } ?: appendLine("-")



        appendLine("--------------------------------")

        appendLine("Water Footprint")

        appendLine()

        appendLine(

            WaterImpactClassifier()

                .classify(

                    entry.waterFootprint

                ).name

        )

        appendLine()

        if (entry.waterFootprint == null) {

            appendLine("-")

        } else {

            appendLine(
                "${entry.waterFootprint.litersPerKilogram} l/kg"
            )

        }
        appendLine()

        appendLine("----------------------------------------")

        appendLine("Ingredients")

        appendLine()

        if (entry.ingredients.isEmpty()) {

            appendLine("-")

        } else {

            entry.ingredients.forEach {

                appendLine("• $it")

            }

        }

        appendLine()

        appendLine("----------------------------------------")
        appendLine()

        appendLine("Recipes")
        appendLine()

        if (entry.recipes.isEmpty()) {

            appendLine("-")

        } else {

            entry.recipes
                .sorted()
                .forEach {

                    appendLine("• $it")

                }

        }

        appendLine()

        appendLine("--------------------------------")

        appendLine("Biodiversity")

        appendLine()

        if (entry.biodiversity == null) {

            appendLine("-")

        } else {

            appendLine()

            appendLine(

                BiodiversityClassifier()

                    .classify(

                        entry.biodiversity

                    ).name

            )
            appendLine(
                entry.biodiversity.score.toString()
            )

        }

        appendLine("--------------------------------")

        appendLine("Pollinator Score")

        appendLine()

        if (entry.pollinator == null) {

            appendLine("-")

        } else {

            appendLine(
                entry.pollinator.score.toString()
            )

        }

        appendLine()

        appendLine("Pollinator Level")

        appendLine()

        appendLine(

            PollinatorClassifier()

                .classify(

                    entry.pollinator

                ).name

        )

        appendLine("--------------------------------")

        appendLine("Food Miles")

        appendLine()

        if (entry.foodMiles == null) {

            appendLine("-")

        } else {

            appendLine(
                "${entry.foodMiles.kilometers} km"
            )

        }

        appendLine()

        appendLine("Food Miles Level")

        appendLine()

        appendLine(

            FoodMilesClassifier()

                .classify(

                    entry.foodMiles

                ).name

        )

        appendLine("--------------------------------")

        appendLine("Packaging")

        appendLine()

        if (entry.packaging == null) {

            appendLine("-")

        } else {

            appendLine(
                entry.packaging.score.toString()
            )

        }

        appendLine()

        appendLine("Packaging Impact")

        appendLine()

        appendLine(

            PackagingImpactClassifier()

                .classify(

                    entry.packaging

                ).name

        )

        appendLine("--------------------------------")

        appendLine("Processing")

        appendLine()

        appendLine(

            entry.processing?.displayName ?: "-"

        )

        appendLine("--------------------------------")

        appendLine("Locality")

        appendLine()

        appendLine(

            entry.locality?.displayName ?: "-"

        )

        appendLine("========================================")
        appendLine()

        appendLine("Knowledge Completeness")
        appendLine()

        appendLine(
            "Category          ${
                if (entry.category.name != "UNKNOWN") "✓" else "✗"
            }"
        )

        appendLine(
            "Diet              ${
                if (entry.dietClassifications.isNotEmpty()) "✓" else "✗"
            }"
        )

        appendLine(
            "Glycemic          ${
                if (entry.glycemicIndex.name != "UNKNOWN") "✓" else "✗"
            }"
        )

        appendLine(
            "Allergens         ✓"
        )

        appendLine(
            "Production        ${
                if (entry.production.isNotEmpty()) "✓" else "✗"
            }"
        )

        appendLine(
            "Tags              ${
                if (entry.tags.isNotEmpty()) "✓" else "✗"
            }"
        )

        appendLine(
            "Seasonality       ${
                if (entry.seasonality.isNotEmpty()) "✓" else "✗"
            }"
        )

        appendLine(
            "Carbon            ${
                if (entry.carbonFootprint != null) "✓" else "✗"
            }"
        )

        appendLine(
            "Ingredients       ${
                if (entry.ingredients.isNotEmpty()) "✓" else "✗"
            }"
        )

        appendLine(
            "Recipes           ${
                if (entry.recipes.isNotEmpty()) "✓" else "✗"
            }"
        )

        appendLine()

    }

}