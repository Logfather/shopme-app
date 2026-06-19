package de.shopme.data.nutrition.localfallback

import de.shopme.domain.nutrition.model.NutritionProduct

object OfflineNutritionDataSource {

    fun getProduct(
        reference: String
    ): NutritionProduct? {

        return when (reference.lowercase()) {

            "apfel" -> NutritionProduct(
                barcode = "embedded-apfel",
                name = "Apfel",
                brand = null,
                category = "Obst & Gemüse",
                imageUrl = null,
                nutrition = OfflineNutritionInfoProvider.apfel()
            )

            "banane" -> NutritionProduct(
                barcode = "embedded-banane",
                name = "Banane",
                brand = null,
                category = "Obst & Gemüse",
                imageUrl = null,
                nutrition = OfflineNutritionInfoProvider.banane()
            )

            "butter" -> NutritionProduct(
                barcode = "embedded-butter",
                name = "Butter",
                brand = null,
                category = "Molkereiprodukte & Eier",
                imageUrl = null,
                nutrition = OfflineNutritionInfoProvider.butter()
            )

            "coca_cola" -> NutritionProduct(
                barcode = "embedded-coca-cola",
                name = "Coca Cola",
                brand = "Coca Cola",
                category = "Getränke",
                imageUrl = null,
                nutrition = OfflineNutritionInfoProvider.cocaCola()
            )

            "nutella" -> NutritionProduct(
                barcode = "embedded-nutella",
                name = "Nutella",
                brand = "Ferrero",
                category = "Saucen & Aufstriche",
                imageUrl = null,
                nutrition = OfflineNutritionInfoProvider.nutella()
            )

            "vollmilch" -> NutritionProduct(
                barcode = "embedded-vollmilch",
                name = "Vollmilch",
                brand = null,
                category = "Molkereiprodukte & Eier",
                imageUrl = null,
                nutrition = OfflineNutritionInfoProvider.vollmilch()
            )

            else -> null
        }
    }
}