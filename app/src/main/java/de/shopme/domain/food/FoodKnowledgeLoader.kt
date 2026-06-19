package de.shopme.domain.food

interface FoodKnowledgeLoader {

    fun load():
            Map<String, FoodKnowledgeEntry>

}