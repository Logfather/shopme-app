package de.shopme.tools.knowledge.allergen

interface AllergenResolver {

    fun resolve(
        foodReference: String?
    ): Set<Allergen>

}