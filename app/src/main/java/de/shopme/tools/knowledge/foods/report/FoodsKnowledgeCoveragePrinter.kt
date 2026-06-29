package de.shopme.tools.knowledge.foods.report

class FoodsKnowledgeCoveragePrinter {

    fun print(
        report: FoodsKnowledgeCoverageReport
    ) {

        println()
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("🧠 CANONICAL FOODS COVERAGE")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("📊 Foods : ${report.totalFoods}")
        println()

        printLine("Nutrition", report.nutrition, report.totalFoods)
        printLine("Carbon", report.carbon, report.totalFoods)
        printLine("Water", report.water, report.totalFoods)
        printLine("Water Stress", report.waterStress, report.totalFoods)
        printLine("Biodiversity", report.biodiversity, report.totalFoods)
        printLine("Pollinator", report.pollinator, report.totalFoods)
        printLine("Pesticides", report.pesticide, report.totalFoods)

        printLine("Production", report.production, report.totalFoods)
        printLine("Processing", report.processing, report.totalFoods)
        printLine("Packaging", report.packaging, report.totalFoods)
        printLine("Locality", report.locality, report.totalFoods)
        printLine("Food Miles", report.foodMiles, report.totalFoods)
        printLine("Fair Trade", report.fairTrade, report.totalFoods)
        printLine("Animal Welfare", report.animalWelfare, report.totalFoods)

        printLine("Ingredients", report.ingredients, report.totalFoods)
        printLine("Allergens", report.allergens, report.totalFoods)
        printLine("Taxonomy", report.taxonomy, report.totalFoods)
        printLine("Seasonality", report.seasonality, report.totalFoods)

        printLine("Diet", report.dietClassifications, report.totalFoods)
        printLine("Nutri Score", report.nutriScore, report.totalFoods)
        printLine("Carbon Impact", report.carbonImpact, report.totalFoods)
        printLine("Glycemic Index", report.glycemicIndex, report.totalFoods)

        printLine("Ingredient Graph", report.ingredientGraph, report.totalFoods)
        printLine("Recipe Graph", report.recipeGraph, report.totalFoods)
        printLine("Recipes", report.recipes, report.totalFoods)

        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println()
    }

    private fun printLine(
        label: String,
        value: Int,
        total: Int
    ) {
        val percent =
            if (total == 0) {
                0.0
            } else {
                value * 100.0 / total
            }

        println(
            "${label.padEnd(18)}: $value / $total (${String.format("%.2f", percent)} %)"
        )
    }
}