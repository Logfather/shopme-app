package de.shopme.domain.food

interface FoodKnowledgeProvider {

    fun find(
        normalizedName: String
    ): FoodKnowledgeEntry?

}