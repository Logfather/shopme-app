package de.shopme.tools.knowledge.taxonomy

interface FoodTaxonomyResolver {

    fun resolve(
        foodReference: String?
    ): FoodTaxonomyEntry?

}