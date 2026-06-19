package de.shopme.domain.food

interface FoodClassifier {

    fun classify(
        productName: String
    ): FoodCategory

}