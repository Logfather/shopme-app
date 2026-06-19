package de.shopme.tools.knowledge.foodcategory

import de.shopme.domain.food.FoodCategory

class DefaultFoodCategoryResolver : FoodCategoryResolver {

    override fun resolve(
        supermarketCategory: String?
    ): FoodCategory {

        return when (supermarketCategory) {

            "Obst & Gemüse" ->
                FoodCategory.FRUIT

            "Fleisch & Wurst" ->
                FoodCategory.PROTEIN

            "Fisch & Meeresfrüchte" ->
                FoodCategory.PROTEIN

            "Molkereiprodukte & Eier" ->
                FoodCategory.DAIRY

            "Backwaren" ->
                FoodCategory.GRAIN

            "Backzutaten & Backmischungen" ->
                FoodCategory.GRAIN

            "Nudeln, Reis & Getreide" ->
                FoodCategory.GRAIN

            "Getränke" ->
                FoodCategory.BEVERAGE

            "Kaffee & Tee" ->
                FoodCategory.BEVERAGE

            "Konserven & Fertiggerichte" ->
                FoodCategory.PROCESSED

            "Tiefkühlprodukte" ->
                FoodCategory.PROCESSED

            "Snacks & Süßwaren" ->
                FoodCategory.SWEETS

            else ->
                FoodCategory.UNKNOWN

        }

    }

}