package de.shopme.tools.knowledge.allergen

class DefaultAllergenResolver(

    private val knowledge: AllergenKnowledge

) : AllergenResolver {

    override fun resolve(
        foodReference: String?
    ): Set<Allergen> {

        val reference =
            foodReference ?: return emptySet()

        return knowledge.entries[
            reference.lowercase()
        ] ?: emptySet()

    }

}