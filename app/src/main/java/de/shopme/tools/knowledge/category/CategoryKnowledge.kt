package de.shopme.tools.knowledge.category

data class CategoryKnowledge(

    val categories: List<CategoryMapping>

)
{
    fun asLookupMap(): Map<String, String> {

        return categories
            .flatMap { category ->

                category.products.map { product ->

                    product.lowercase() to category.name

                }

            }
            .toMap()

    }
}