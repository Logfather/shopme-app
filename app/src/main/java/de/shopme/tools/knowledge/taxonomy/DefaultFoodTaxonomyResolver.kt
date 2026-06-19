package de.shopme.tools.knowledge.taxonomy

class DefaultFoodTaxonomyResolver(

    private val knowledge: FoodTaxonomyKnowledge

) : FoodTaxonomyResolver {

    override fun resolve(

        foodReference: String?

    ): FoodTaxonomyEntry? {

        val reference =

            foodReference ?: return null

        return knowledge.entries[

            reference.lowercase()

        ]

    }

}