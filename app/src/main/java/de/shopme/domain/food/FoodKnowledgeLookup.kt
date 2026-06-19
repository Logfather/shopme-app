package de.shopme.domain.food

interface FoodKnowledgeLookup {

    fun find(
        normalizedName: String
    ): FoodKnowledgeEntry?

}