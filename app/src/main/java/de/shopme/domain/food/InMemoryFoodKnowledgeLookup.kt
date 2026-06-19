package de.shopme.domain.food

class InMemoryFoodKnowledgeLookup(

    private val entries:
    Map<String, FoodKnowledgeEntry>

) : FoodKnowledgeLookup {

    override fun find(
        normalizedName: String
    ): FoodKnowledgeEntry? {

        return entries[
            normalizedName.lowercase()
        ]

    }

}