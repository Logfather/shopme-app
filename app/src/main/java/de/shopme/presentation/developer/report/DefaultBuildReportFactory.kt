package de.shopme.presentation.developer.report

class DefaultBuildReportFactory : BuildReportFactory {

    override fun create(): BuildReport {

        return BuildReportBuilder()

            .section("FACT") {

                entry("Nutrition", 20)
                entry("Glycemic", 19)
                entry("Carbon", 20)
                entry("Water", 20)
                entry("WaterStress", 19)
                entry("Biodiversity", 20)
                entry("Pollinator", 10)
                entry("Pesticides", 9)
                entry("AnimalWelfare", 7)
                entry("FairTrade", 8)
                entry("Packaging", 14)
                entry("FoodMiles", 15)
                entry("Locality", 12)
                entry("Production", 7)
                entry("Seasonality", 53)
                entry("FoodTaxonomy", 33)
                entry("Ingredients", 53)
                entry("Allergens", 29)

            }

            .section("GRAPH") {

                entry("IngredientGraph", 4)
                entry("RecipeGraph", 1)

            }

            .section("INTERPRETATION") {

                entry("Diet", 5)
                entry("CarbonImpact", 20)
                entry("NutriScore", 19)

            }

            .build()

    }

}